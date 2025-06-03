package com.ljm.message;

import lombok.AllArgsConstructor;

/**
 * 心跳检测
 * @author ljm
 */
@AllArgsConstructor
public enum RequestType {

    NORMAL(0),

    HEARTBEAT(1);

    private int code;

    public int getCode() {
        return code;
    }
}
