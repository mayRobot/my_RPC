package com.chuang.rpc.serializer;

/**
 * 序列化器接口
 *
 * */
public interface Serializer {
    // 序列化对象，返回byte数组
    byte[] serialize(Object object);
    // 反序列化，根据指定类型和数据，返回内容为数据的类对象
    Object deserialize(byte[] bytes, Class<?> clazz);
    // 获取序列化器的编号
    int getCode();
    // 通过编号获取序列化器
    static Serializer getSerializerByCode(int code){
        switch (code){
            case 0:
                return new KryoSerializer();
            case 1:
                return new JsonSerializer();
            default:
                return null;
        }
    }
}
