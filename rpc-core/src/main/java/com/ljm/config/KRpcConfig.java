package com.ljm.config;


import lombok.*;

/**
 * @ClassName KRpcConfig
 * @Description 配置文件
 * @Author ljm
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
/*public class KRpcConfig {
    //名称
    private String name = "krpc";
    //端口
    private Integer port = 9999;
    //主机名
    private String host = "localhost";
    //版本号
    private String version = "1.0.0";
    //注册中心
    private String registry = new ZKServiceRegister().toString();
    //序列化器
    private String serializer = Serializer.getSerializerByCode(3).toString();
    //负载均衡
    private String loadBalance = new ConsistencyHashBalance().toString();

}*/
public class KRpcConfig {
    //名称
    private String name ;
    //端口
    private Integer port ;
    //主机名
    private String host ;
    //版本号
    private String version ;
    //注册中心
    private String registry ;
    //序列化器
    private String serializer;
    //负载均衡
    private String loadBalance ;
}

