package com.ljm.server.rateLimit.provider;

import com.ljm.server.rateLimit.RateLimit;
import com.ljm.server.rateLimit.impl.TokenBucketRateLimitImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 提供限流器实例
 * @author ljm
 */
@Slf4j
public class RateLimitProvider {
    private final Map<String, RateLimit> rateLimitMap=new ConcurrentHashMap<>();

    // 默认的限流桶容量和令牌生成速率
    private static final int DEFAULT_CAPACITY = 100;
    private static final int DEFAULT_RATE = 100;
    public RateLimit getRateLimit(String interfaceName) {
        return rateLimitMap.computeIfAbsent(interfaceName, key -> {
            RateLimit rateLimit = new TokenBucketRateLimitImpl(DEFAULT_RATE, DEFAULT_CAPACITY);
            log.info("为接口 [{}] 创建了新的限流策略: {}", interfaceName, rateLimit);
            return rateLimit;
        });
    }
}
