package com.chuang.rpc.socket.utils;

import com.chuang.rpc.entity.RpcRequest;
import com.chuang.rpc.enumeration.PackageType;
import com.chuang.rpc.serializer.Serializer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 辅助类，用于socket方式下提供序列化器
 * 写
 * 按照codec中的协议，将各部分内容转化为bytes，并分批write入outputStream中
 * 对于需要序列化的部分，调用指定序列化器的序列化方法即可
 *
 * 现在socket的发送协议与netty相同，则二者可通信
 * */
public class ObjectWriter {

    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    public static void writeObject(OutputStream outputStream, Object object, Serializer serializer) throws IOException {
        // 1、写入魔数
        outputStream.write(intToBytes(MAGIC_NUMBER));
        // 2、写入包类型
        if (object instanceof RpcRequest) {
            outputStream.write(intToBytes(PackageType.REQUEST_PACK.getCode()));
        } else {
            outputStream.write(intToBytes(PackageType.RESPONSE_PACK.getCode()));
        }
        // 3、序列化类型：当前使用的序列化器的getCode获得
        outputStream.write(intToBytes(serializer.getCode()));
        // 4、数据长度：先序列化数据，根据byte数组信息
        byte[] bytes = serializer.serialize(object);
        outputStream.write(intToBytes(bytes.length));
        // 5、写入数据
        outputStream.write(bytes);
        outputStream.flush();

    }

    /*
    * Java中intToBytes是将高位放在数组靠左，转换后顺序从左到右，位数依次降低
    * 相应的bytesToInt则是将bytes按顺序还原，数组左侧还原完成后继续左移
    * */
    private static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[0] =  (byte) ((value>>24) & 0xFF);
        src[1] =  (byte) ((value>>16) & 0xFF);
        src[2] =  (byte) ((value>>8) & 0xFF);
        src[3] =  (byte) (value & 0xFF);
        return src;
    }
}
