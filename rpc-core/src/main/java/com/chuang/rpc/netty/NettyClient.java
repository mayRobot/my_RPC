package com.chuang.rpc.netty;

import com.chuang.rpc.codec.CommonDecoder;
import com.chuang.rpc.codec.CommonEncoder;
import com.chuang.rpc.entity.RpcRequest;
import com.chuang.rpc.entity.RpcResponse;
import com.chuang.rpc.enumeration.RPCError;
import com.chuang.rpc.exception.RPCException;
import com.chuang.rpc.interfaces.RpcClient;
import com.chuang.rpc.serializer.KryoSerializer;
import com.chuang.rpc.serializer.Serializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetSocketAddress;


/**
 * Netty方式实现的客户端，实现RpcClient接口
 * 客户端类，将接收到的Request请求发送并返回接收到的结果
 * */
public class NettyClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private String host;
    private int port;
    private Serializer serializer;
    private static final Bootstrap bootstrap;

    /*
     * 在静态代码块中直接配置好
     * */
    static {
        // 线程组，客户端只用一个，而服务端用两个，详见笔记
        EventLoopGroup group = new NioEventLoopGroup();
        // 服务端启动辅助类，为Netty程序启动组装配置一些必要组件
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true);

    }

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        // 配置序列化器
        // 在发送请求前检查是否有序列化器，懒汉式
        if(serializer == null){
            logger.error("未设置序列化器");
            throw new RPCException(RPCError.SERIALIZER_NOT_FOUND);
        }
        // 执行发送请求程序
        try{
            // TODO ChannelFuture功能
//            ChannelFuture future = bootstrap.connect(host, port).sync();
//            logger.info("客户端连接至服务端 {}:{}", host, port);
//
//            // 获取channel
//            Channel channel = future.channel();

            // 获取channel：通过ChannelProvider提供channel，其内部支持失败重连
            Channel channel = ChannelProvider.get(new InetSocketAddress(host, port), serializer);
            if(channel.isActive()){
                // channel将RpcRequest对象flush，并且等待结果返回
                // TODO addListener功能
                channel.writeAndFlush(rpcRequest).addListener(future1 -> {
                    if(future1.isSuccess()){
                        logger.info("客户端成功发送消息：{}", rpcRequest.toString());
                    } else{
                        logger.error("客户端发送消息失败：", future1.cause());
                    }
                });
                // 利用AttributeKey方式阻塞获得返回结果？？？？
                // TODO 查询AttributeKey功能
                /*
                 * 通过这种方式获得全局可见的返回结果，在获得返回结果 RpcResponse 后，
                 * 将这个对象以 key 为 rpcResponse 放入 ChannelHandlerContext 中，这里就可以立刻获得结果并返回，
                 * 会在 NettyClientHandler 中看到放入的过程
                 * */
                channel.closeFuture().sync();
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
                RpcResponse rpcResponse = channel.attr(key).get();
                logger.info("rpcResponse:{}", rpcResponse.toString());
                return rpcResponse.getData();
            }else{
                System.exit(0);
            }

        } catch (InterruptedException e) {
            logger.error("发送消息时失败：", e);
        }
        return null;
    }

    @Override
    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }
}
