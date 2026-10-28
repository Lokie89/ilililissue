package com.ilililissue.common.adapter.in.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 파라미터에 로그인한 회원의 {@code MemberId}를 주입한다.
 * {@code required = false}이면 비로그인 요청에서 null을 주입한다.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginMember {

    boolean required() default true;
}
