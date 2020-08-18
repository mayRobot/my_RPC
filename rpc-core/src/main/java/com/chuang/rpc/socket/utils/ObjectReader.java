package com.chuang.rpc.socket.utils;

import com.chuang.rpc.entity.RpcRequest;
import com.chuang.rpc.entity.RpcResponse;
import com.chuang.rpc.enumeration.PackageType;
import com.chuang.rpc.enumeration.RPCError;
import com.chuang.rpc.exception.RPCException;
import com.chuang.rpc.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * 辅助类，用于socket方式下提供序列化器
 * 读
 * 从socket的InputStream中bytes数组，按照codec中的协议格式分别转化为相应对象，
 * 需要反序列化的内容，参考testSerializer中调用方法，调用反序列化方法实现转化
 *
 * 现在socket的发送协议与netty相同，则二者可通信
 * */
public class ObjectReader {

    private static final Logger logger = LoggerFactory.getLogger(ObjectReader.class);
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    public static Object readObject(InputStream in) throws IOException {
        // 1、校验魔数
        byte[] numberBytes = new byte[4];
        in.read(numberBytes);
        int magic = bytesToInt(numberBytes);
        if (magic != MAGIC_NUMBER) {
            logger.error("无法识别的数据包协议: {}", magic);
            throw new RPCException(RPCError.UNKNOWN_PROTOCOL);
        }
        // 2、校验包类型
        in.read(numberBytes);
        int packageCode = bytesToInt(numberBytes);
        Class<?> packageClass;
        if (packageCode == PackageType.REQUEST_PACK.getCode()) {
            packageClass = RpcRequest.class;
        } else if (packageCode == PackageType.RESPONSE_PACK.getCode()) {
            packageClass = RpcResponse.class;
        } else {
            logger.error("无法识别的数据包类型: {}", packageCode);
            throw new RPCException(RPCError.UNKNOWN_PACKAGE_TYPE);
        }
        // 3、校验序列器类型
        in.read(numberBytes);
        int serializerCode = bytesToInt(numberBytes);
        Serializer serializer = Serializer.getSerializerByCode(serializerCode);
        if (serializer == null) {
            logger.error("无法识别的反序列化器类型：{}", serializerCode);
            throw new RPCException(RPCError.UNKNOWN_SERIALIZER);
        }
        // 4、获取数据
        in.read(numberBytes);
        int length = bytesToInt(numberBytes);
        byte[] bytes = new byte[length];
        in.read(bytes);
        return serializer.deserialize(bytes, packageClass);
    }

    /*
     * Java中intToBytes是将高位放在数组靠左，转换后顺序从左到右，位数依次降低
     * 相应的bytesToInt则是将bytes按顺序还原，数组左侧还原完成后继续左移
     * */
    public static int bytesToInt(byte[] src) {
        int value;
        value = ((src[0] & 0xFF)<<24)
                | ((src[1] & 0xFF)<<16)
                | ((src[2] & 0xFF)<<8)
                | ((src[3] & 0xFF));
        return value;
    }

}
