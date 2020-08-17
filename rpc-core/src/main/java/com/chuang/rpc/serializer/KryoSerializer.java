package com.chuang.rpc.serializer;

import com.chuang.rpc.entity.RpcRequest;
import com.chuang.rpc.entity.RpcResponse;
import com.chuang.rpc.enumeration.SerializerCode;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.ObjenesisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


/*
 * Kryo序列化器
 * Kryo 是一个快速高效的 Java 对象序列化框架，主要特点是高性能、高效和易用。最重要的两个特点，
 * 一是基于字节的序列化，对空间利用率较高，在网络传输时可以减小体积；
 * 二是序列化时记录属性对象的类型信息，这样在反序列化时就不会出现Json序列化器中丢失对象类型的问题
 * */
public class KryoSerializer implements Serializer{

    private static final Logger logger = LoggerFactory.getLogger(KryoSerializer.class);

    //TODO 多线程之ThreadLocal
    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RpcRequest.class);
        kryo.register(RpcResponse.class);
        kryo.setReferences(true);
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    /*
     * 在序列化时，先创建一个 Output 对象（Kryo 框架的概念），
     * 接着使用 writeObject 方法将对象写入 Output 中，
     * 最后调用 Output 对象的 toByte() 方法即可获得对象的字节数组。
    * */
    @Override
    public byte[] serialize(Object object) {
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Output output = new Output(byteArrayOutputStream)){
            //------------------------------------------
            Kryo kryo = kryoThreadLocal.get();
            kryo.writeObject(output, object);
            kryoThreadLocal.remove();
            return output.toBytes();
        } catch (IOException e) {
            logger.error("序列化时错误：{}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /*
    * 反序列化则是从 Input 对象中直接 readObject，这里只需要传入对象的类型，而不需要具体传入每一个属性的类型信息。
    * */
    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            Input input = new Input(byteArrayInputStream)){

            Kryo kryo = kryoThreadLocal.get();
            Object object = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();
            return object;
        } catch (IOException e) {
            logger.error("反序列化时发生错误：{}", e.getMessage());
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public int getCode() {
        return SerializerCode.valueOf("KRYO").getCode();
    }
}
