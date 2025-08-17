package com.ljm.serializer.mycode;


import com.ljm.message.MessageType;
import com.ljm.message.RpcRequest;
import com.ljm.message.RpcResponse;
import com.ljm.serializer.myserializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName MyEncoder
 * @Description 编码器
 * @Author ljm
 */
@Slf4j
@AllArgsConstructor
public class MyEncoder extends MessageToByteEncoder {//MessageToByteEncoder是netty专门设计用来实现编码器得抽象类，可以帮助开发者将Java对象编码成字节数据。
    private Serializer serializer;

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        log.debug("编码的消息类型为: {}", msg.getClass());
        //1.写入消息类型
        if (msg instanceof RpcRequest) {
            out.writeShort(MessageType.REQUEST.getCode());
        } else if (msg instanceof RpcResponse) {
            out.writeShort(MessageType.RESPONSE.getCode());
        } else {
            log.error("Unknown message type: {}", msg.getClass());
            throw new IllegalArgumentException("未知消息类型: " + msg.getClass());
        }
        //2.写入序列化方式
        out.writeShort(serializer.getType());
        //得到序列化数组
        byte[] serializeBytes = serializer.serialize(msg);
        if (serializeBytes == null || serializeBytes.length == 0) {
            throw new IllegalArgumentException("被序列化的消息为空");
        }
        //3.写入长度
        out.writeInt(serializeBytes.length);
        //4.写入序列化数组
        out.writeBytes(serializeBytes);
    }
}
