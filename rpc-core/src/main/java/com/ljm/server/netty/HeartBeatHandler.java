package com.ljm.server.netty;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务端心跳检测器
 * @author ljm
 * 服务端关注读空闲和写空闲事件
 */
@Slf4j
public class HeartBeatHandler extends ChannelDuplexHandler {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        try {
            if(evt instanceof IdleStateEvent){
                IdleStateEvent event = (IdleStateEvent) evt;
                IdleState state = event.state();
                //读空闲
                if(state==IdleState.READER_IDLE){
                    log.info("超过12秒没有收到客户端心跳,channel:"+ctx.channel());
                    //关闭通道
                    ctx.close();
                }else if(state==IdleState.WRITER_IDLE){//写空闲
                    log.info("超过20s没有写数据,channel: " + ctx.channel());
                    // 关闭channel，避免造成更多资源占用
                    ctx.close();
                }
            }
        } catch (Exception e) {
            log.error("服务端心跳处理异常:{}",e.getMessage());
        }
    }
}
