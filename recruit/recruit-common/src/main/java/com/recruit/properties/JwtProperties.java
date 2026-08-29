package com.recruit.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 招聘系统 JWT 配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "recruit.jwt")
public class JwtProperties {

    private String secretKey;
    private long ttl;
    private String tokenName;
}
