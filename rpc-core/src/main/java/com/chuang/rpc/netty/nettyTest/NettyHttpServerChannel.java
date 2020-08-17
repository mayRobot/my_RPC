package com.chuang.rpc.netty.nettyTest;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

// Channel初始化类
public class NettyHttpServerChannel extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        // 处理http消息的编解码,Netty 自带的 Http 编解码组件 HttpServerCodec 对通信数据进行编解码
        // 等同于下面两句合体
        //pipeline.addLast("httpResponseEndcoder", new HttpResponseEncoder()); 用于解码request
        //pipeline.addLast("HttpRequestDecoder", new HttpRequestDecoder()); 用于编码response
        pipeline.addLast("httpServerCodec", new HttpServerCodec());

        // 消息聚合器，使得FullHttpRequest存在，从而有FullHttpResponce
        // 参数代表聚合的消息的最大长度，512 * 1024即512kb
        pipeline.addLast("httpServerAggregator", new HttpObjectAggregator(512 * 1024));
        // 添加自定义的ChannelHandler，用于处理业务逻辑
        pipeline.addLast("httpServerHandler", new NettyHttpHandler());
        /*
        * ch.pipeline()
                                .addLast("decoder", new HttpRequestDecoder())   // 1
                                .addLast("encoder", new HttpResponseEncoder())  // 2
                                .addLast("aggregator", new HttpObjectAggregator(512 * 1024))    // 3
                                .addLast("handler", new HttpHandler());        // 4
        * */
    }
}
