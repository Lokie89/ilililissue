package com.ilililissue.issue.domain.vo;

import com.ilililissue.common.SelfValidating;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 이슈 출처 목록. 같은 URL은 한 번만 가진다.
 */
@EqualsAndHashCode(callSuper = false)
public class IssueSources extends SelfValidating<IssueSources> {

    @NotNull
    private final List<IssueSource> values;

    private IssueSources(
            Collection<IssueSource> values
    ) {
        this.values = List.copyOf(new LinkedHashSet<>(values));
        this.validate();
    }

    public static IssueSources from(IssueSource... sources) {
        return new IssueSources(Arrays.asList(sources));
    }

    public static IssueSources from(Collection<IssueSource> sources) {
        return new IssueSources(sources);
    }

    public static IssueSources empty() {
        return new IssueSources(Collections.emptyList());
    }

    public boolean isEmpty() {
        return this.values.isEmpty();
    }

    public List<IssueSource> getValues() {
        return this.values;
    }
}
