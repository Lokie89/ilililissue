package com.ilililissue.issue.domain.vo;

import com.ilililissue.common.SelfValidating;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author SeongRok.Oh
 * @since 2023/08/21
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class IssueImages extends SelfValidating<IssueImages> {

    @NotEmpty
    private final Set<IssueImage> values;

    public static IssueImages from(IssueImage... issueImages) {
        return new IssueImages(Arrays.stream(issueImages).collect(Collectors.toSet()));
    }

    public static IssueImages from(Collection<IssueImage> issueImages) {
        return new IssueImages(new HashSet<>(issueImages));
    }

}
