package com.chuang.rpc.codec;

import com.chuang.rpc.entity.RpcRequest;
import com.chuang.rpc.enumeration.PackageType;
import com.chuang.rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义的编码器，在发送的数据上加上各种必要的数据，形成自定义的协议，最终将发送的信息转化为byte数组
 *
 * 仿照http协议，数据格式为：
 * 1、4 byte 魔数 magicNumber
 * 2、4 byte 包类型 packageType 表面是请求还是响应
 * 3、4 byte 序列化类型 serializerType 实际使用的序列化器，接收、发送双方应统一
 * 4、4 byte 数据长度 dataLength 数据所用长度，防止粘包
 * 5、data 长度为 dataLength
 * */
public class CommonEncoder extends MessageToByteEncoder {
    private static final Logger logger = LoggerFactory.getLogger(CommonEncoder.class);

    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    private final Serializer serializer;

    public CommonEncoder(Serializer serializer) {
        this.serializer = serializer;
    }

    // 变量命名采用常见命名 ctx表示ChannelHandlerContext部分，msg表示要处理的对象，byteBuf用于输出，以out代表
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        // 1、写入魔数
        out.writeInt(MAGIC_NUMBER);
        // 2、写入包类型
        // 有些像NettyTest未引入FullHttpRequest时对request和content分别处理
        if(msg instanceof RpcRequest)
            out.writeInt(PackageType.REQUEST_PACK.getCode());
        else
            out.writeInt(PackageType.RESPONSE_PACK.getCode());
        // 3、序列化类型：当前使用的序列化器的getCode获得
        out.writeInt(serializer.getCode());
        // 4、数据长度：先序列化数据，根据byte数组信息
        byte[] data = serializer.serialize(msg);
        out.writeInt(data.length);
        // 5、写入数据
        out.writeBytes(data);
        logger.info("编码器：{} 编码结束", serializer.getClass());
    }
}
