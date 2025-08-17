package com.ljm.server.serverRegister;


import java.net.InetSocketAddress;

/**
 * @InterfaceName ServiceRegister
 * @Description 服务注册接口
 * @Author ljm
 * @LastChangeDate 2025-07-01 10:40
 * @Version v5.0
 */

public interface ServiceRegister {
    void register(Class<?> clazz, InetSocketAddress serviceAddress);
}
