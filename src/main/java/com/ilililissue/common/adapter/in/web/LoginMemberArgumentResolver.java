package com.ilililissue.common.adapter.in.web;

import com.ilililissue.common.DomainException;
import com.ilililissue.member.domain.vo.MemberId;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class)
                && MemberId.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return new MemberId(Long.parseLong(jwt.getSubject()));
        }
        if (parameter.getParameterAnnotation(LoginMember.class).required()) {
            throw new LoginRequiredException();
        }
        return null;
    }

    static class LoginRequiredException extends DomainException {

        LoginRequiredException() {
            super(ErrorType.UNAUTHORIZED, "로그인이 필요합니다.");
        }
    }
}
