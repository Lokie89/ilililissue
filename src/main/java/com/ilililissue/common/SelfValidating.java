package com.ilililissue.common;

import jakarta.validation.*;

import java.util.Set;


/**
 * @author SeongRok.Oh
 * @since 2023/08/18
 */
public abstract class SelfValidating<T> {
    private final Validator validator;
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    public SelfValidating() {
        this.validator = factory.getValidator();
    }

    protected void validate() {
        Set<ConstraintViolation<SelfValidating<T>>> violations = this.validator.validate(this);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
