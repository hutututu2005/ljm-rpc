package com.ljm.client.circuitbreaker;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ljm
 * @description 提供熔断器
 */
@Slf4j
public class CircuitBreakerProvider {
    private Map<String,CircuitBreaker> circuitBreakerMap= new ConcurrentHashMap<>();

    public synchronized CircuitBreaker getCircuitBreaker(String serviceName) {
        return circuitBreakerMap.computeIfAbsent(serviceName, k -> {
            log.info("服务 [{}] 不存在熔断器，创建新的熔断器实例", serviceName);
            return  new CircuitBreaker(2,0.5,10,10000);
        });
    }
}
