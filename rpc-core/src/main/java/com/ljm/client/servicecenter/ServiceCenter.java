package com.ljm.client.servicecenter;


import com.ljm.message.RpcRequest;

import java.net.InetSocketAddress;

/**
 * @InterfaceName ServiceCenter
 * @Description 服务中心接口
 * @Author ljm
 * @LastChangeDate 2025-07-01 10:31
 * @Version v5.0
 */

public interface ServiceCenter {
    //服务发现：根据服务名查找地址
    InetSocketAddress serviceDiscovery(RpcRequest request);

    //判断是否可重试
    boolean checkRetry(InetSocketAddress serviceAddress, String methodSignature);

    //关闭客户端
    void close();
}
