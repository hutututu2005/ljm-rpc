package com.ljm.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @ClassName RpcResponse
 * @Description 定义响应消息格式
 * @Author ljm
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class RpcResponse implements Serializable {
    //状态信息
    private int code;
    private String message;
    private Class<?> dataType;
    private Object data;

    public static RpcResponse sussess(Object data) {
        return RpcResponse.builder().code(200).dataType(data.getClass()).data(data).build();
    }

    public static RpcResponse fail(String msg) {
        return RpcResponse.builder().code(500).message(msg).build();
    }
}
