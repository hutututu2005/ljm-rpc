package com.ljm.client.circuitbreaker;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ljm
 * @description 熔断器
 */
@Slf4j
public class CircuitBreaker {

    //熔断器当前状态,默认关闭
    private CircuitBreakerState state = CircuitBreakerState.CLOSED;
    private AtomicInteger failureCount = new AtomicInteger(0);//请求失败次数
    private AtomicInteger successCount = new AtomicInteger(0);//请求成功次数
    private AtomicInteger requestCount = new AtomicInteger(0);//请求次数
    //关闭-》开启阈值  开启-》半开阈值
    private final int failureThreshold;
    private final double halfOpenThreshold;
    // 最小请求数阈值（避免少量请求导致误判）
    private final int minRequestThreshold;
    //检测开启-》半开的时间周期
    private final long retryTimePeriod;
    //上次失败时间
    private long lastFailureTime = 0;
    public CircuitBreaker(int failureThreshold, double halfOpenThreshold, int minRequestThreshold,long retryTimePeriod) {
        this.failureThreshold = failureThreshold;
        this.halfOpenThreshold = halfOpenThreshold;
        this.retryTimePeriod = retryTimePeriod;
        this.minRequestThreshold = minRequestThreshold;
    }

    public synchronized boolean allowRequest() {
        long currentTime = System.currentTimeMillis();
        log.info("熔断检查, 当前失败次数：{}", failureCount);
        switch (state){
            case OPEN :
                if(currentTime-lastFailureTime>retryTimePeriod){
                    state = CircuitBreakerState.HALF_OPEN;
                    resetCounts();
                    log.info("熔断已解除，进入半开启状态，允许请求通过");
                    requestCount.incrementAndGet();
                    return true;
                }
                log.warn("熔断生效中，拒绝请求！");
                return false;
            case HALF_OPEN:
                requestCount.incrementAndGet();
                log.info("当前为半开启状态，计数请求+1");
                return true;
            case CLOSED:
            default:
                log.info("当前为正常状态，允许请求通过");
                return true;
        }
    }
    //记录请求成功
    public synchronized void recordSuccess() {
        if(state==CircuitBreakerState.HALF_OPEN){
            int success = successCount.incrementAndGet();
            int total = requestCount.get();
            //达到转换阈值
            if(total >= minRequestThreshold&& success >= total*halfOpenThreshold){
                state = CircuitBreakerState.CLOSED;
                //状态转换需要重置次数
                resetCounts();
                log.info("成功次数已达到阈值，熔断器切换至关闭状态");
            }
        }
        else {
            resetCounts();
            log.info("熔断器处于关闭状态，重置计数器");
        }
    }
    //记录请求失败
    public synchronized void recordFailure() {
        int failure = failureCount.incrementAndGet();
        lastFailureTime = System.currentTimeMillis();
        if(state==CircuitBreakerState.HALF_OPEN){
            state = CircuitBreakerState.OPEN;//半开状态下失败，熔断重新打开
            log.warn("半开启状态下发生失败，熔断器切换至开启状态");
        }else if(failure>=failureThreshold){
            state = CircuitBreakerState.OPEN; // 失败超过阈值，切换到打开状态
            log.error("失败次数已超过阈值，熔断器切换至开启状态");
        }
    }
    //重置次数
    private void resetCounts() {
        failureCount.set(0);
        successCount.set(0);
        requestCount.set(0);
    }

    public CircuitBreakerState getState() {
        return state;
    }

    enum CircuitBreakerState {
        CLOSED, // 关闭状态
        OPEN, // 开启状态
        HALF_OPEN // 半开状态
    }
}

