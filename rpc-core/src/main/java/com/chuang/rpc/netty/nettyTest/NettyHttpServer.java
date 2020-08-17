package com.chuang.rpc.netty.nettyTest;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * netty实现httpServer
 * 启动类
 * */
public class NettyHttpServer {
    private final int port;

    public NettyHttpServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        new NettyHttpServer(9000).start();
    }

    public void start(){
        // 线程组，可以理解为线程池，默认线程数是CPU核心树*2，bossgroup负责接收客户端传来的请求，workerGroup则负责后续处理
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            // 服务端启动辅助类，为Netty程序启动组装配置一些必要组件
            ServerBootstrap bootstrap = new ServerBootstrap();

            /*
            * group中放入之前创建的线程组
            * channel用于指定服务器端监听套接字通道NioServerSocketChannel，其内部管理了一个Java NIO的ServerSocketChannel实例
            * childHandler用于设置业务责任链，实际上就是一个个的ChannelHandler串联而成的链式结构，
            * 也正是这些ChannelHandler完成了业务处理
            *
            * */
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NettyHttpServerChannel());

            // bind方法将服务绑定到具体端口，内部会执行端口绑定等一系列操作，sync方法用于阻塞当前线程，直到bind方法结束
            ChannelFuture future = bootstrap.bind(port).sync();
            // 程序阻塞，直到服务端口关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            // 释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }


    }
}
