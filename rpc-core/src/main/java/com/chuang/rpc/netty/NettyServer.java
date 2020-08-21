package com.chuang.rpc.netty;

import com.chuang.rpc.codec.CommonDecoder;
import com.chuang.rpc.codec.CommonEncoder;
import com.chuang.rpc.enumeration.RPCError;
import com.chuang.rpc.exception.RPCException;
import com.chuang.rpc.hook.ShutdownHook;
import com.chuang.rpc.interfaces.RpcServer;
import com.chuang.rpc.provider.DefaultServiceProvider;
import com.chuang.rpc.provider.ServiceProvider;
import com.chuang.rpc.registry.NacosServiceRegistry;
import com.chuang.rpc.registry.ServiceRegistry;
import com.chuang.rpc.serializer.JsonSerializer;
import com.chuang.rpc.serializer.KryoSerializer;
import com.chuang.rpc.serializer.Serializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Set;

/**
 * Netty方式实现的服务端，实现RpcServer接口
 * 服务端类，监听某端口，循环接收连接请求，如果收到请求就创建一个线程，并调用相关方法处理请求
 * */
public class NettyServer implements RpcServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    // 本服务端的host和port，用于经Nacos注册中心传递给将调用本服务端的客户端
    // 也因为作为服务端，不清楚何时会有怎样的客户端访问，因此，将host和port固化，有利于减少复杂度
    private final String host;
    private final int port;
    // V3.0：因为需要将当前服务端及所含服务注册到中心，增加serviceRegistry，serviceProvider则用于保存当前所含服务
    private final ServiceRegistry serviceRegistry;
    private final ServiceProvider serviceProvider;

    private Serializer serializer;

    public NettyServer(String host, int port) {
        this.host = host;
        this.port = port;
        this.serviceProvider = new DefaultServiceProvider();
        this.serviceRegistry = new NacosServiceRegistry();
    }


    @Override
    public <T> void publishService(Object serviceObject, Class<?> serviceClass) {
        if(serializer == null){
            logger.error("未设置序列化器");
            throw new RPCException(RPCError.SERIALIZER_NOT_FOUND);
        }
        serviceProvider.addService(serviceObject);
        serviceRegistry.register(serviceClass.getCanonicalName(), new InetSocketAddress(host, port));
    }

    @Override
    public void start() {
        if(serializer == null){
            logger.error("未设置序列化器");
            throw new RPCException(RPCError.SERIALIZER_NOT_FOUND);
        }

        // 线程组，可以理解为线程池，默认线程数是CPU核心树*2，bossgroup负责接收客户端传来的请求，workerGroup则负责后续处理
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try{
            // 服务端启动辅助类，为Netty程序启动组装配置一些必要组件
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>(){
                        // channel的初始化类
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 通过pipeline添加责任链
                            ChannelPipeline pipeline = socketChannel.pipeline();

                            // 使用自定义的编码器、解码器、处理类
                            pipeline.addLast("encoder", new CommonEncoder(serializer));
                            pipeline.addLast("decoder", new CommonDecoder());
                            pipeline.addLast("handler", new NettyServerHandler(serviceProvider));

                        }
                    });
            // 绑定port端口，并阻塞当前程序待绑定完成
            ChannelFuture future = bootstrap.bind(host, port).sync();

            // 加钩子，实现服务端关闭前自动注销服务
            ShutdownHook.getShutdownHook().addClearAllHook(this);

            // 程序阻塞，直到服务端口关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("启动服务器发生错误：", e);
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public InetSocketAddress getAddress() {
        return new InetSocketAddress(host, port);
    }

    @Override
    public Set<String> getAllServices() {
        return serviceProvider.getAllServices();
    }
}
