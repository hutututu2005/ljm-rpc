package com.ljm.serializer.myserializer;


import com.ljm.exception.SerializeException;
import com.ljm.message.RpcRequest;
import com.ljm.message.RpcResponse;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @ClassName ProtostuffSerializer
 * @Description protostuff序列化
 * @Author ljm
 */

/**
 * Protostuff 是基于 Google Protocol Buffers（ protobuf）的高性能 Java 序列化框架，
 * 通过运行时反射或字节码生成技术实现高效的对象序列化
 */
public class ProtostuffSerializer implements Serializer {
    //缓存schema，避免每次序列化都要重新获取schema
    private static final Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();
    //类型映射注册表
    private static final Map<Integer,Class<?>> typeMap = new HashMap<>();
    //每个线程独立使用对象池，避免多线程问题
    private final ThreadLocal<LinkedBuffer> bufferThreadLocal = ThreadLocal.withInitial(() -> LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));

    static {
        //注册类型映射
        typeMap.put(0, RpcRequest.class);
        typeMap.put(1, RpcResponse.class);
       // typeMap.put(1, User.class); 测试用例
    }
    @Override
    public byte[] serialize(Object obj) {
        if(obj==null){
            throw new IllegalArgumentException("无法序列化空对象");
        }
        Class<?> clazz = obj.getClass();
        Schema schema = getSchema(clazz);
        LinkedBuffer buffer = bufferThreadLocal.get();
        try {
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            buffer.clear();
        }
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Cannot deserialize null or empty byte array");
        }

        Class<?> clazz = typeMap.get(messageType);
        if (clazz == null) {
            throw new SerializeException("Unknown message type: " + messageType);
        }

        Schema schema = getSchema(clazz);

        try {
            // 使用 schema.newMessage() 替代反射
            Object obj = schema.newMessage();
            ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
            return obj;
        } catch (Exception e) {
            throw new SerializeException("Deserialization failed: " + e.getMessage(), e);
        }
    }
   @SuppressWarnings("unchecked")
   private <T> Schema<T> getSchema(Class<T> clazz) {
        //有则复用，无则创建
        return (Schema<T>) cachedSchema.computeIfAbsent(clazz, RuntimeSchema::getSchema);
   }
    @Override
    public int getType() {
        return 4;
    }

    @Override
    public String toString() {
        return "Protostuff";
    }
}
