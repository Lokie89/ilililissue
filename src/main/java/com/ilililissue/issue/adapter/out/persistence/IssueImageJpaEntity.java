package com.ilililissue.issue.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author SeongRok.Oh
 * @since 2023/08/21
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "issue_image")
@Entity
public class IssueImageJpaEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String fileName;

    private Long issueId;

}
