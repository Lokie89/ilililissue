package com.ilililissue.metrics.adapter.in.web;

import com.ilililissue.auth.config.AuthProperties;
import com.ilililissue.common.adapter.in.web.LoginMember;
import com.ilililissue.issue.application.port.in.IssueQuery;
import com.ilililissue.issue.domain.vo.IssueId;
import com.ilililissue.member.domain.vo.MemberId;
import com.ilililissue.metrics.application.port.in.RecordEventUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 이슈 상세 화면을 연 브라우저가 호출하는 조회 기록 API.
 * <p>
 * 방문자는 {@value #VISITOR_ID} 쿠키(임의 UUID, 1년)로 구분한다. 쿠키가 없거나 형식이 틀리면 새로 발급한다.
 * 앱은 설치할 때 만든 UUID를 {@value #VISITOR_HEADER} 헤더로 보낸다. 헤더가 유효하면 쿠키보다 우선하고 쿠키를 발급하지 않는다.
 * 개인을 식별하는 정보는 담지 않으며 지표의 고유 방문자 수를 세는 데만 쓴다.
 */
@RequiredArgsConstructor
@RestController
public class IssueViewController {

    public static final String VISITOR_ID = "visitor_id";
    public static final String VISITOR_HEADER = "X-Visitor-Id";
    private static final Duration VISITOR_TTL = Duration.ofDays(365);
    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

    private final IssueQuery issueQuery;
    private final RecordEventUseCase recordEventUseCase;
    private final AuthProperties authProperties;

    @PostMapping("/api/issues/{issueId}/views")
    public ResponseEntity<Void> view(@PathVariable long issueId,
                                     @LoginMember(required = false) MemberId memberId,
                                     @CookieValue(name = VISITOR_ID, required = false) String cookieVisitorId,
                                     @RequestHeader(name = VISITOR_HEADER, required = false) String headerVisitorId) {
        IssueId id = new IssueId(issueId);
        issueQuery.getPublicIssue(id);

        if (isValid(headerVisitorId)) {
            recordEventUseCase.issueViewed(id, memberId, headerVisitorId.toLowerCase());
            return ResponseEntity.noContent().build();
        }
        boolean valid = isValid(cookieVisitorId);
        String visitor = valid ? cookieVisitorId : UUID.randomUUID().toString();
        recordEventUseCase.issueViewed(id, memberId, visitor);

        ResponseEntity.HeadersBuilder<?> response = ResponseEntity.noContent();
        if (!valid) {
            response.header(HttpHeaders.SET_COOKIE, ResponseCookie.from(VISITOR_ID, visitor)
                    .httpOnly(true)
                    .secure(authProperties.cookieSecure())
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(VISITOR_TTL)
                    .build()
                    .toString());
        }
        return response.build();
    }

    private static boolean isValid(String visitorId) {
        return visitorId != null && UUID_PATTERN.matcher(visitorId.toLowerCase()).matches();
    }
}
