package com.ilililissue.metrics.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * @param eventRetention 참여 이벤트(이용 기록) 보관 기간. 지나면 매일 새벽에 지운다.
 *                       개인정보 처리방침(web/src/app/privacy/page.tsx)의 보관 기간과 맞춘다.
 */
@ConfigurationProperties("metrics")
public record MetricsProperties(
        Duration eventRetention
) {

    public MetricsProperties {
        eventRetention = eventRetention == null ? Duration.ofDays(365) : eventRetention;
        if (eventRetention.isNegative() || eventRetention.isZero()) {
            throw new IllegalStateException("metrics.event-retention은 0보다 커야 합니다.");
        }
    }
}
