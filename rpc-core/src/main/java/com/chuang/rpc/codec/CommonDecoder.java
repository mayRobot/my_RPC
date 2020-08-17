package com.chuang.rpc.codec;

import com.chuang.rpc.entity.RpcRequest;
import com.chuang.rpc.entity.RpcResponse;
import com.chuang.rpc.enumeration.PackageType;
import com.chuang.rpc.enumeration.RPCError;
import com.chuang.rpc.exception.RPCException;
import com.chuang.rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 解码器，将序列化后的数据按照协议还原为对象
 *
 * 仿照http协议，数据格式为：
 * 1、4 byte 魔数 magicNumber
 * 2、4 byte 包类型 packageType 表面是请求还是响应
 * 3、4 byte 序列化类型 serializerType 实际使用的序列化器，接收、发送双方应统一
 * 4、4 byte 数据长度 dataLength 数据所用长度，防止粘包
 * 5、data 长度为 dataLength
 *
 * RepalyingEncode是bytemessagedecoder的一个特殊变化，继承了阻塞IO中的非阻塞解码器；
 *
 * 它俩最大的区别就是在实现decode()和decodeLast()方法之后RepalyingEncode不需要检查请求字节是否可行
 * 当数据足够的时候，会执行后面的操作；如果数据不够的话会抛出错误异常
 * */
public class CommonDecoder extends ReplayingDecoder {
    private static final Logger logger = LoggerFactory.getLogger(CommonDecoder.class);

    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    // 变量命名采用常见命名 ctx表示ChannelHandlerContext部分，byteBuf属于输入，以in代表，list的内容用于输出out
    // 解码，并逐步校验各部分是否正确，若有错误，就抛出相应异常
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 1、校验魔数
        int magicNumber = in.readInt();
        if(magicNumber != MAGIC_NUMBER){
            logger.error("无法识别的数据包协议: {}", magicNumber);
            throw new RPCException(RPCError.UNKNOWN_PROTOCOL);
        }
        // 2、校验包类型
        int packageCode = in.readInt();
        Class<?> packageClass;
        if (packageCode == PackageType.REQUEST_PACK.getCode())
            packageClass = RpcRequest.class;
        else if(packageCode == PackageType.RESPONSE_PACK.getCode())
            packageClass = RpcResponse.class;
        else{
            logger.error("无法识别的数据包类型：{}", packageCode);
            throw new RPCException(RPCError.UNKNOWN_PACKAGE_TYPE);
        }
        // 3、校验序列器类型
        int serializerCode = in.readInt();
        Serializer serializer = Serializer.getSerializerByCode(serializerCode);
        if(serializer == null){
            logger.error("无法识别的（反）序列化器类型：{}", serializerCode);
            throw new RPCException(RPCError.UNKNOWN_SERIALIZER);
        }
        // 4、获取数据
        int length = in.readInt();
        byte[] data = new byte[length];
        in.readBytes(data);
        // 反序列化，还原成之前的对象
        Object obj = serializer.deserialize(data, packageClass);
        out.add(obj);
    }
}
