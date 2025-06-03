package com.ljm.server.rateLimit.provider;

import com.ljm.server.rateLimit.RateLimit;
import com.ljm.server.rateLimit.impl.TokenBucketRateLimitImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 提供限流器实例
 * @author ljm
 */
public class RateLimitProvider {
    private final Map<String, RateLimit> rateLimitMap=new ConcurrentHashMap<>();

    // 默认的限流桶容量和令牌生成速率
    private static final int DEFAULT_CAPACITY = 10;
    private static final int DEFAULT_RATE = 100;
    public RateLimit getRateLimit(String interfaceName) {
        if(!rateLimitMap.containsKey(interfaceName)){
            RateLimit rateLimit = new TokenBucketRateLimitImpl(DEFAULT_RATE, DEFAULT_CAPACITY);
            rateLimitMap.put(interfaceName, rateLimit);
            return rateLimit;
        }
        return rateLimitMap.get(interfaceName);
    }
}
