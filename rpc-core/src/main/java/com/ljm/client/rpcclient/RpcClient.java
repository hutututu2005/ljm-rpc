package com.ljm.client.rpcclient;


import com.ljm.message.RpcRequest;
import com.ljm.message.RpcResponse;

/**
 * @InterfaceName RpcClient
 * @Description 定义底层通信方法
 * @Author ljm
 * @LastChangeDate 2024-12-02 10:11
 * @Version v5.0
 */

public interface RpcClient {
    RpcResponse sendRequest(RpcRequest request);
    void close();
}
