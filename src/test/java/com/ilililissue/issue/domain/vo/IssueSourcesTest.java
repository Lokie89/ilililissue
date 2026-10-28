package com.ilililissue.issue.domain.vo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

public class IssueSourcesTest {

    private static final LocalDate PUBLISHED_ON = LocalDate.of(2026, 10, 27);

    @DisplayName("같은 URL의 출처는 한 번만 가진다")
    @Test
    void distinctByUrl() {
        IssueSources sources = IssueSources.from(
                IssueSource.of("https://news.example.com/a/1", "예시일보", PUBLISHED_ON),
                IssueSource.of("https://news.example.com/a/1", "다른이름", PUBLISHED_ON),
                IssueSource.of("https://news.example.com/a/2", "예시일보", PUBLISHED_ON)
        );

        Assertions.assertThat(sources.getValues()).hasSize(2);
    }

    @DisplayName("http/https가 아닌 URL, 언론사나 발행일이 없는 출처는 허용하지 않는다")
    @Test
    void invalidSource() {
        Assertions.assertThatThrownBy(() -> IssueSource.of("ftp://news.example.com/a", "예시일보", PUBLISHED_ON));
        Assertions.assertThatThrownBy(() -> IssueSource.of("news.example.com/a", "예시일보", PUBLISHED_ON));
        Assertions.assertThatThrownBy(() -> IssueSource.of("https://news.example.com/a", " ", PUBLISHED_ON));
        Assertions.assertThatThrownBy(() -> IssueSource.of("https://news.example.com/a", "예시일보", null));
    }
}
