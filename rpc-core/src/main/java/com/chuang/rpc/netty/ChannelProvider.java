package com.chuang.rpc.netty;

import com.chuang.rpc.codec.CommonDecoder;
import com.chuang.rpc.codec.CommonEncoder;
import com.chuang.rpc.enumeration.RPCError;
import com.chuang.rpc.exception.RPCException;
import com.chuang.rpc.serializer.Serializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 提供客户端连接失败重连功能，连接成功后，返回channel
 *
 * */
public class ChannelProvider {

    private static final Logger logger = LoggerFactory.getLogger(ChannelProvider.class);

    private static EventLoopGroup eventLoopGroup;
    private static Bootstrap bootstrap;

    //在静态代码块中直接配置好
    static {
        // 线程组，客户端只用一个，而服务端用两个，详见笔记
        eventLoopGroup = new NioEventLoopGroup();
        // 服务端启动辅助类，为Netty程序启动组装配置一些必要组件
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                // 连接的超时时间
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法
                .option(ChannelOption.TCP_NODELAY, true);
                // TCP底层心跳机制，没用。。。
                //.option(ChannelOption.SO_KEEPALIVE, true);
    }

    private static final int MAX_RETRY_TIMES = 5;
    private static Channel channel = null;

    public static Channel get(InetSocketAddress inetSocketAddress, Serializer serializer){
        //InetAddress:类的主要作用是封装IP及DNS，InetSocketAddress类主要作用是封装端口 是在InetAddress基础上加端口
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();

                // 使用自定义的解码器、编码器、处理类
                pipeline.addLast("decoder", new CommonDecoder())
                        .addLast("encoder", new CommonEncoder(serializer))
                        .addLast("handler", new NettyClientHandler());

//                        pipeline.addLast("decoder", new ObjectDecoder(ClassResolvers.cacheDisabled(
//                                this.getClass().getClassLoader()
//                        )))
//                                .addLast("encoder", new ObjectEncoder())
//                                .addLast("handler", new NettyClientHandler());
            }
        });

        CountDownLatch countDownLatch = new CountDownLatch(1);

        try {
            connect(bootstrap, inetSocketAddress, MAX_RETRY_TIMES, countDownLatch);
            countDownLatch.await();//没有连接成功，latch不为0，就等待，除非抛出异常
        } catch (InterruptedException e) {
            logger.error("获取channel时发生错误：", e);
        }
        return channel;
    }

    // bootstrap获取inetSocketAddress处的链接，可继续尝试次数为retryCount，提供相应countDownLatch用于get中交互
    private static void connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress, int retryCount, CountDownLatch countDownLatch){
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if(future.isSuccess()){
                logger.info("客户端连接至服务端 {}:{}", inetSocketAddress.getHostString(), inetSocketAddress.getPort());
                channel = future.channel();//获取channel
                countDownLatch.countDown();
                return;
            }
            if(retryCount == 0){
                logger.error("客户端连接服务端失败，重试次数已用完，放弃连接！");
                throw new RPCException(RPCError.CLIENT_CONNECT_SERVER_FAILED);
            }
            // 判断是第几次重连，延长重新请求的时间，再次请求
            int count = (MAX_RETRY_TIMES - retryCount) + 1;
            int delay = 1<<count;
            logger.error("{}连接失败，第 {} 次重连...", new Date(), count);
            bootstrap.config().group().schedule(() -> connect(bootstrap, inetSocketAddress, retryCount-1, countDownLatch),
                    delay, TimeUnit.SECONDS);
        });
    }

}
