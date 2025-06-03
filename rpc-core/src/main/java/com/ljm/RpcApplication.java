package com.ljm;

import com.ljm.config.KRpcConfig;
import com.ljm.config.RpcConstant;
import com.ljm.utils.ConfigUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName RpcApplication
 * @Description 测试配置顶
 * @Author ljm
 */
@Slf4j
public class RpcApplication {
    private static volatile KRpcConfig rpcConfigInstance;

    public static void initialize(KRpcConfig customRpcConfig) {
        rpcConfigInstance = customRpcConfig;
        log.info("RPC 框架初始化，配置 = {}", customRpcConfig);
    }

    public static void initialize() {
        KRpcConfig customRpcConfig;
        try {
            customRpcConfig = ConfigUtil.loadConfig(KRpcConfig.class, RpcConstant.CONFIG_FILE_PREFIX);
            log.info("成功加载配置文件，配置文件名称 = {}", RpcConstant.CONFIG_FILE_PREFIX); // 添加成功加载的日志
        } catch (Exception e) {
            // 配置加载失败，使用默认配置
            customRpcConfig = new KRpcConfig();
            log.warn("配置加载失败，使用默认配置");
        }
        initialize(customRpcConfig);
    }

    public static KRpcConfig getRpcConfig() {
        if (rpcConfigInstance == null) {
            synchronized (RpcApplication.class) {
                if (rpcConfigInstance == null) {
                    initialize();  // 确保在第一次调用时初始化
                }
            }
        }
        return rpcConfigInstance;
    }
}
