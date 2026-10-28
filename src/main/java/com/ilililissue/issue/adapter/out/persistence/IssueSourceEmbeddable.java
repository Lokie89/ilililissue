package com.ilililissue.issue.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class IssueSourceEmbeddable {

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(nullable = false, length = 50)
    private String publisher;

    @Column(nullable = false)
    private LocalDate publishedOn;
}
