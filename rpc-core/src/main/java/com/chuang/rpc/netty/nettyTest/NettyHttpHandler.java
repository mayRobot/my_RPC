package com.chuang.rpc.netty.nettyTest;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;

/**
 * 自己定义的处理类，用于业务处理
 * 声明为FullHttpRequest，使得只有FullHttpRequest的消息才能进来
 *
 * Netty 的设计中把 Http 请求分为了 HttpRequest 和 HttpContent 两个部分，
 * HttpRequest 主要包含请求头、请求方法等信息，HttpContent 主要包含请求体的信息。
 * 可以通过Unpooled提供的静态辅助方法创建未池化的ByteBuffer实例，然后创建FullHttpResponse 的实例，并为它设置一些响应参数，最后通过 writeAndFlush 方法将它写回给客户端
 *
 *
 * 上述过程太不方便，Netty提供了FullHttpRequest接口，包含了请求的所有信息，直接或间接继承了HttpRequest和HttpContent
 * 实现类是 DefalutFullHttpRequest
 * */
public class NettyHttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private AsciiString contentType = HttpHeaderValues.TEXT_PLAIN;

    // 服务器接收到数据会调用该方法
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        System.out.println("class:" + fullHttpRequest.getClass().getName());
        // 生成FullHttpResponse，就无需将response拆分成多个channel返回给请求端了
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK, Unpooled.wrappedBuffer("test".getBytes()));

        HttpHeaders heads = response.headers();
        heads.add(HttpHeaderNames.CONTENT_TYPE, contentType + "; charset=UTF-8");
        heads.add(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes()); // 确定header长度，防止请求端postman不断刷新
        heads.add(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

        channelHandlerContext.write(response);
        // 下文不刷新的问题可以加这句解决
        //channelHandlerContext.writeAndFlush(response);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelReadComplete");
        super.channelReadComplete(ctx);
        ctx.flush(); // 不加这句，postman会不断刷新
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("exceptionCaught");
        if(cause != null) cause.printStackTrace();
        if(ctx != null) ctx.close();

    }


}
