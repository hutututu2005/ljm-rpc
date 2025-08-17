package com.ljm.server.netty;


import com.ljm.serializer.mycode.MyDecoder;
import com.ljm.serializer.mycode.MyEncoder;
import com.ljm.serializer.myserializer.Serializer;
import com.ljm.server.provider.ServiceProvider;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.AllArgsConstructor;

import java.util.concurrent.TimeUnit;


/**
 * @ClassName NettyServerInitializer
 * @Description 服务端初始化器
 * @Author ljm
 * @LastChangeDate 2025-07-01 10:40
 * @Version v5.0
 */
@AllArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    private ServiceProvider serviceProvider;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        //使用自定义的编/解码器
        pipeline.addLast(new MyEncoder(Serializer.getSerializerByCode(3)));
        pipeline.addLast(new MyDecoder());
        pipeline.addLast(new NettyRpcServerHandler(serviceProvider));
        //心跳检测
        pipeline.addLast(new IdleStateHandler(12,20,0, TimeUnit.SECONDS));
        pipeline.addLast(new HeartBeatHandler());
    }
}
