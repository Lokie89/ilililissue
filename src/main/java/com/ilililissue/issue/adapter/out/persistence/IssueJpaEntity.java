package com.ilililissue.issue.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author SeongRok.Oh
 * @since 2023/08/21
 */

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "issue")
@Entity
public class IssueJpaEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @OneToMany(mappedBy = "issue_id")
    private List<IssueImageJpaEntity> issueImageJpaEntityList;

    @Column(nullable = false)
    private Long userId;

}
