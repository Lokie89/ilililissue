package com.ilililissue.issue.adapter.out.file;

import com.ilililissue.issue.application.port.out.IssueImageStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 서버 폴더({@code issue.image-dir})에 {@code <키>.jpg}로 저장한다. 서버 한 대 전제이며, 운영(Docker)에서는 볼륨으로 둔다.
 */
@Slf4j
@Component
public class FileIssueImageStore implements IssueImageStore {

    private static final Pattern KEY = Pattern.compile("^[a-f0-9]{32}$");

    private final Path dir;

    public FileIssueImageStore(@Value("${issue.image-dir:data/issue-images}") String dir) {
        this.dir = Path.of(dir).toAbsolutePath();
    }

    @Override
    public String save(byte[] jpeg) {
        String key = UUID.randomUUID().toString().replace("-", "");
        try {
            Files.createDirectories(dir);
            Files.write(dir.resolve(key + ".jpg"), jpeg);
        } catch (IOException e) {
            throw new UncheckedIOException("이슈 이미지를 저장하지 못했습니다: " + dir, e);
        }
        return key;
    }

    @Override
    public Optional<byte[]> load(String key) {
        Path file = fileOf(key);
        if (file == null || !Files.isRegularFile(file)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readAllBytes(file));
        } catch (IOException e) {
            throw new UncheckedIOException("이슈 이미지를 읽지 못했습니다: " + file, e);
        }
    }

    @Override
    public void delete(String key) {
        Path file = fileOf(key);
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("이슈 이미지를 지우지 못했습니다: {}", file, e);
        }
    }

    // 키는 저장할 때 만든 형식만 받는다(경로 조작 방지).
    private Path fileOf(String key) {
        return key != null && KEY.matcher(key).matches() ? dir.resolve(key + ".jpg") : null;
    }
}
