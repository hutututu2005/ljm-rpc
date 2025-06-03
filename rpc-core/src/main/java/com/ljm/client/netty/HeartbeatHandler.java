package com.ljm.client.netty;

import com.esotericsoftware.minlog.Log;
import com.ljm.message.RpcRequest;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @author ljm
 * @description 客户端心跳处理器
 */
public class HeartbeatHandler extends ChannelDuplexHandler {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            IdleState state = event.state();
            //客户端只关注写空闲事件，如果在10秒内没有任何写操作，将会触发写空闲事件，向服务端发送心跳包。
            if(state == IdleState.READER_IDLE){
                ctx.writeAndFlush(RpcRequest.heartBeat());
                Log.info("客户端超过10秒没写数据，发送心跳包");
            }
        }else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
