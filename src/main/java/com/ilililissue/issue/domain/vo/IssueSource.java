package com.ilililissue.issue.domain.vo;

import com.ilililissue.common.SelfValidating;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 이슈의 근거가 된 원문 기사 링크. 기사 본문과 사진은 보관하지 않는다.
 */
@Getter
@EqualsAndHashCode(of = "url", callSuper = false)
public class IssueSource extends SelfValidating<IssueSource> {

    @NotBlank
    @Size(max = 2048)
    @Pattern(regexp = "^https?://\\S+$")
    private final String url;

    @NotBlank
    @Size(max = 50)
    private final String publisher;

    @NotNull
    private final LocalDate publishedOn;

    private IssueSource(
            String url,
            String publisher,
            LocalDate publishedOn
    ) {
        this.url = url;
        this.publisher = publisher;
        this.publishedOn = publishedOn;
        this.validate();
    }

    public static IssueSource of(String url, String publisher, LocalDate publishedOn) {
        return new IssueSource(url, publisher, publishedOn);
    }
}
