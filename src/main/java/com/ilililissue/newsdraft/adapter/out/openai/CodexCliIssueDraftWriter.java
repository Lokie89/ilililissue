package com.ilililissue.newsdraft.adapter.out.openai;

import com.ilililissue.issue.domain.entity.Category;
import com.ilililissue.newsdraft.application.NewsDraftException;
import com.ilililissue.newsdraft.application.port.out.IssueDraftWriterPort;
import com.ilililissue.newsdraft.config.NewsDraftProperties;
import com.ilililissue.newsdraft.domain.IssueDraft;
import com.ilililissue.newsdraft.domain.NewsArticle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * <h2>로컬 테스트용: Codex CLI로 이슈 초안 쓰기</h2>
 * {@code news-draft.writer=codex-cli}일 때 쓴다. 서버가 도는 PC에 설치·로그인된 {@code codex}(ChatGPT 계정)를 실행하므로
 * API 키가 필요 없고 ChatGPT 요금제 사용 한도를 쓴다. 운영 서버 자동 실행에는 OpenAI API 키 방식(기본)을 쓴다.
 * <ul>
 * <li>인터페이스: {@code codex exec --skip-git-repo-check --ephemeral --sandbox read-only -C <빈 임시 폴더>
 * --output-schema <스키마 파일> -o <결과 파일> -}, 프롬프트는 표준 입력(UTF-8).</li>
 * <li>빈 임시 폴더에서 읽기 전용 샌드박스로 실행해 저장소나 다른 파일을 건드리지 않게 하고, 끝나면 폴더를 지운다.</li>
 * </ul>
 */
@Slf4j
@ConditionalOnProperty(name = "news-draft.writer", havingValue = "codex-cli")
@Component
public class CodexCliIssueDraftWriter implements IssueDraftWriterPort {

    private static final long TIMEOUT_MINUTES = 10;
    private static final int LOG_TAIL_LENGTH = 2000;

    private final NewsDraftProperties properties;
    private final JsonMapper jsonMapper;

    public CodexCliIssueDraftWriter(NewsDraftProperties properties, JsonMapper jsonMapper) {
        this.properties = properties;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public List<IssueDraft> write(List<NewsArticle> articles, List<Category> categories, List<String> recentTitles, int draftsPerCategory) {
        String prompt = IssueDraftPrompt.INSTRUCTIONS
                + "\n도구나 명령을 실행하지 말고, 아래 입력만 보고 바로 최종 답을 JSON으로 주세요.\n\n"
                + IssueDraftPrompt.input(articles, categories, recentTitles, draftsPerCategory);
        Path dir = null;
        try {
            dir = Files.createTempDirectory("news-draft-codex");
            Path work = Files.createDirectory(dir.resolve("work"));
            Path schema = dir.resolve("schema.json");
            Path result = dir.resolve("last-message.json");
            Path log = dir.resolve("codex.log");
            Files.writeString(schema, jsonMapper.writeValueAsString(IssueDraftPrompt.DRAFTS_SCHEMA));

            Process process = new ProcessBuilder(command(work, schema, result))
                    .redirectErrorStream(true)
                    .redirectOutput(log.toFile())
                    .start();
            try (OutputStream stdin = process.getOutputStream()) {
                stdin.write(prompt.getBytes(StandardCharsets.UTF_8));
            }
            if (!process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
                process.destroyForcibly();
                throw NewsDraftException.externalFailure("Codex CLI가 " + TIMEOUT_MINUTES + "분 안에 끝나지 않았습니다.");
            }
            if (process.exitValue() != 0 || !Files.exists(result)) {
                throw NewsDraftException.externalFailure("Codex CLI 실행에 실패했습니다(종료 코드 " + process.exitValue() + "): "
                        + tail(log));
            }
            return IssueDraftPrompt.toDrafts(jsonMapper.readTree(Files.readString(result)));
        } catch (IOException e) {
            throw NewsDraftException.externalFailure("Codex CLI를 실행할 수 없습니다(" + properties.codexCommand() + "): " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw NewsDraftException.externalFailure("Codex CLI 실행이 중단되었습니다.");
        } finally {
            deleteQuietly(dir);
        }
    }

    private List<String> command(Path work, Path schema, Path result) {
        List<String> command = new ArrayList<>();
        // Windows의 npm 설치본은 codex.cmd라 cmd.exe를 거쳐야 실행된다.
        if (System.getProperty("os.name", "").toLowerCase().startsWith("windows")) {
            command.addAll(List.of("cmd.exe", "/c"));
        }
        command.addAll(List.of(properties.codexCommand(), "exec",
                "--skip-git-repo-check", "--ephemeral", "--sandbox", "read-only",
                "-C", work.toString(),
                "--output-schema", schema.toString(),
                "-o", result.toString()));
        if (properties.codexModel() != null && !properties.codexModel().isBlank()) {
            command.addAll(List.of("-m", properties.codexModel()));
        }
        command.add("-");
        return command;
    }

    private static String tail(Path log) {
        try {
            String text = Files.readString(log);
            return text.length() > LOG_TAIL_LENGTH ? text.substring(text.length() - LOG_TAIL_LENGTH) : text;
        } catch (IOException e) {
            return "(로그 없음)";
        }
    }

    private static void deleteQuietly(Path dir) {
        if (dir == null) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
        } catch (IOException ignored) {
            // 임시 폴더라 지우지 못해도 다음 실행에 영향이 없다.
        }
    }
}
