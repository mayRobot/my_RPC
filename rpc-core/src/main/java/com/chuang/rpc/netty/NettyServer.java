package com.chuang.rpc.netty;

import com.chuang.rpc.codec.CommonDecoder;
import com.chuang.rpc.codec.CommonEncoder;
import com.chuang.rpc.interfaces.RpcServer;
import com.chuang.rpc.serializer.JsonSerializer;
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

/**
 * Netty方式实现的服务端，实现RpcServer接口
 * 服务端类，监听某端口，循环接收连接请求，如果收到请求就创建一个线程，并调用相关方法处理请求
 * */
public class NettyServer implements RpcServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    @Override
    public void start(int port) {
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
//                            pipeline.addLast("encoder", new CommonEncoder(new JsonSerializer()));
//                            pipeline.addLast("decoder", new CommonDecoder());
                            pipeline.addLast("encoder", new ObjectEncoder());
                            pipeline.addLast("decoder", new ObjectDecoder(ClassResolvers.cacheDisabled(
                                    this.getClass().getClassLoader()
                            )));
                            pipeline.addLast("handler", new NettyServerHandler());
                        }
                    });
            // 绑定port端口，并阻塞当前程序待绑定完成
            ChannelFuture future = bootstrap.bind(port).sync();
            // 程序阻塞，直到服务端口关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("启动服务器发生错误：", e);
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
