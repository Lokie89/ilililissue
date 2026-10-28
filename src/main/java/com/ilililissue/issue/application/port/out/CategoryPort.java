package com.ilililissue.issue.application.port.out;

import com.ilililissue.issue.domain.entity.Category;
import com.ilililissue.issue.domain.vo.CategoryId;

import java.util.List;
import java.util.Optional;

public interface CategoryPort {

    List<Category> findAll();

    Optional<Category> findById(CategoryId id);

    boolean existsByName(String name);

    Category save(Category category);
}
