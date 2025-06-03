package com.ljm.server.rateLimit.impl;

import com.ljm.server.rateLimit.RateLimit;
import lombok.extern.slf4j.Slf4j;

/**
 * @author  ljm
 * 服务限流
 * 令牌桶算法
 */
@Slf4j
public class TokenBucketRateLimitImpl implements RateLimit {
   //临牌产生速率 每xx ms生成一个
    private final int rate;
    //桶容量
    private final int capacity;
    //当前令牌数量 使用volatile保证可见性
    private volatile int currentTokens;
    //上一次令牌生成时间
    private volatile long lastTimestamp;

    public TokenBucketRateLimitImpl(int rate, int capacity) {
        this.rate = rate;
        this.capacity = capacity;
        this.currentTokens = capacity;
        this.lastTimestamp = System.currentTimeMillis();
    }
    @Override
    public boolean getToken() {
        synchronized (this) {
            //计算当前令牌
            long currentTimestamp = System.currentTimeMillis();
            int newTokens =(int)(currentTimestamp-lastTimestamp)/rate;
            //更新桶中的令牌
            currentTokens = Math.min(currentTokens + newTokens, capacity);
            //更新令牌生成时间
            lastTimestamp = currentTimestamp;
            if(currentTokens >0){
                currentTokens--;
                return true;
            }
            return false;
        }
    }
}
