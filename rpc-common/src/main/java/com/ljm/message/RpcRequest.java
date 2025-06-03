package com.ljm.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @ClassName RpcRequest
 * @Description 定义请求消息格式
 * @Author ljm
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class RpcRequest implements Serializable {
    //请求类型：与心跳包进行区分
    private RequestType requestType=RequestType.NORMAL;
    //接口名、方法名、参数列表参数类型
    private String interfaceName;

    private String methodName;

    private Object[] params;

    private Class<?>[] paramsType;

    public static RpcRequest heartBeat(){
        return RpcRequest.builder().requestType(RequestType.HEARTBEAT).build();
    }
}
