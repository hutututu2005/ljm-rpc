package com.ljm.server.rateLimit;

/**
 * 服务限流
 */
public interface RateLimit {
    //获取访问许可
    boolean getToken();
}
