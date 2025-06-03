package com.ljm.serializer.myserializer;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.ljm.exception.SerializeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @ClassName HessianSerializer
 * @Description Hessian序列化
 * @Author ljm
 */

/**
 * Hessian 序列化性能高的核心原因：
 * 二进制编码：
 *  避免文本解析和转义开销，数据体积更小
 *  直接映射数据类型，无需字符串到二进制的转换
 * 元数据优化：
 *  类定义只写一次，后续对象通过 ID 引用
 *  字段名仅在类定义中出现，实例只需按序写入值
 * 类型处理高效：
 *  整数按范围动态选择编码长度（1-8 字节）
 *  字符串使用长度前缀 + UTF-8 编码
 *  集合类型直接编码长度和元素类型
 * 反射开销低：
 *  预生成序列化器或优化反射调用
 *  直接访问字段而非通过 ObjectOutputStream
 * 循环引用处理：
 *  通过对象 ID 机制避免重复序列化相同对象
 *  处理复杂对象图时效率显著高于 Java 原生序列化
 * 这些设计使 Hessian 在序列化速度、反序列化速度和内存占用上均优于 JSON 和 Java 原生序列化，尤其适合高性能 RPC 和分布式系统。
 */
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        // 使用 ByteArrayOutputStream 和 HessianOutput 来实现对象的序列化
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            HessianOutput hessianOutput = new HessianOutput(byteArrayOutputStream);
            hessianOutput.writeObject(obj);  // 将对象写入输出流
            return byteArrayOutputStream.toByteArray();  // 返回字节数组
        } catch (IOException e) {
            throw new SerializeException("Serialization failed");
        }
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        // 使用 ByteArrayInputStream 和 HessianInput 来实现反序列化
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
            HessianInput hessianInput = new HessianInput(byteArrayInputStream);
            return hessianInput.readObject();  // 读取并返回对象
        } catch (IOException e) {
            throw new SerializeException("Deserialization failed");
        }
    }

    @Override
    public int getType() {
        return 3;
    }

    @Override
    public String toString() {
        return "Hessian";
    }
}