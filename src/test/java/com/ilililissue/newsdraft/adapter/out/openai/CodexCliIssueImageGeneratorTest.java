package com.ilililissue.newsdraft.adapter.out.openai;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

class CodexCliIssueImageGeneratorTest {

    @DisplayName("PNG를 가로 1200px JPEG로 줄인다(비율 유지)")
    @Test
    void toJpeg() throws Exception {
        BufferedImage png = new BufferedImage(1672, 941, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(png, "png", out);

        byte[] jpeg = CodexCliIssueImageGenerator.toJpeg(out.toByteArray());

        BufferedImage result = ImageIO.read(new ByteArrayInputStream(jpeg));
        Assertions.assertThat(jpeg[0]).isEqualTo((byte) 0xFF);
        Assertions.assertThat(jpeg[1]).isEqualTo((byte) 0xD8);
        Assertions.assertThat(result.getWidth()).isEqualTo(1200);
        Assertions.assertThat(result.getHeight()).isEqualTo(675);
    }

    @DisplayName("프롬프트에 이미지 규칙과 주제를 데이터로 넣는다")
    @Test
    void prompt() {
        String prompt = CodexCliIssueImageGenerator.prompt("질문인가?", "배경 설명", "경제");

        Assertions.assertThat(prompt)
                .contains("issue-image.png", "실존 인물", "사진처럼", "<topic>", "분야: 경제", "질문: 질문인가?");
    }
}
