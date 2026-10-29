package com.ilililissue.issue.domain.vo;

import com.ilililissue.common.SelfValidating;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * <h2>이슈를 한눈에 보여주는 요점</h2>
 * <ul>
 * <li>핵심 쟁점: 무엇을 두고 의견이 갈리는지 한 줄.</li>
 * <li>입장별 근거: 찬성 측과 반대(신중) 측의 대표 근거. 한쪽만 보이지 않도록 둘 다 있거나 둘 다 없어야 한다.</li>
 * <li>모두 선택이다. 없으면 배경 설명만 보여준다.</li>
 * </ul>
 */
@Getter
@EqualsAndHashCode(callSuper = false)
public class IssueFocus extends SelfValidating<IssueFocus> {

    private static final IssueFocus EMPTY = new IssueFocus(null, null, null);

    @Size(max = 100)
    private final String keyPoint;

    @Size(max = 300)
    private final String agreeReason;

    @Size(max = 300)
    private final String disagreeReason;

    private IssueFocus(
            String keyPoint,
            String agreeReason,
            String disagreeReason
    ) {
        this.keyPoint = blankToNull(keyPoint);
        this.agreeReason = blankToNull(agreeReason);
        this.disagreeReason = blankToNull(disagreeReason);
        this.validate();
    }

    public static IssueFocus of(String keyPoint, String agreeReason, String disagreeReason) {
        return new IssueFocus(keyPoint, agreeReason, disagreeReason);
    }

    public static IssueFocus empty() {
        return EMPTY;
    }

    @AssertTrue(message = "찬성 측과 반대 측 근거는 둘 다 쓰거나 둘 다 비워야 합니다.")
    private boolean isBalanced() {
        return (agreeReason == null) == (disagreeReason == null);
    }

    public boolean hasReasons() {
        return agreeReason != null;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
