package com.ljm.consumer;


import com.ljm.config.KRpcConfig;
import com.ljm.utils.ConfigUtil;



/**
 * @ClassName ConsumerTestConfig
 * @Description 测试配置顶
 * @Author ljm
 */
public class ConsumerTestConfig {
    public static void main(String[] args) {
        KRpcConfig rpc = ConfigUtil.loadConfig(KRpcConfig.class, "rpc");
        System.out.println(rpc);
    }

}
