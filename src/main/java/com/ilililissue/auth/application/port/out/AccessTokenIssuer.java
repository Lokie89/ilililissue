package com.ilililissue.auth.application.port.out;

import com.ilililissue.member.domain.entity.Member;

import java.time.Duration;
import java.time.Instant;

public interface AccessTokenIssuer {

    String issue(Member member, Instant issuedAt, Duration ttl);
}
