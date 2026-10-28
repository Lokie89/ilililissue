package com.ilililissue.issue.domain.entity;

import com.ilililissue.common.SelfValidating;
import com.ilililissue.issue.domain.vo.CategoryId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * <h2>카테고리 도메인</h2>
 * 이슈 분류 (예: 경제, 사회, 생활). 목록은 관리자가 운영 데이터로 관리한다.
 */
@Getter
@EqualsAndHashCode(of = "id", callSuper = false)
public class Category extends SelfValidating<Category> {

    private final CategoryId id;

    @NotBlank
    @Size(max = 20)
    private final String name;

    private Category(
            CategoryId id,
            String name
    ) {
        this.id = id;
        this.name = name;
        this.validate();
    }

    public static Category of(String name) {
        return new Category(null, name);
    }

    public static Category withId(CategoryId id, String name) {
        return new Category(id, name);
    }
}
