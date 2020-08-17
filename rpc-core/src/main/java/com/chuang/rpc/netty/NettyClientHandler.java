package com.chuang.rpc.netty;

import com.chuang.rpc.entity.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端业务处理类，扩展SimpleChannelInboundHandler
 *
 * 处于责任链尾部，无需关心编解码、序列化的问题，直接处理响应对象response
 * 因为要处理返回的response，因此限定为RpcResponse
 * 接收RpcResponse，获取其中data部分
 * */
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) throws Exception {
        try{
            logger.info("[NettyClientHandler]：客户端接收到服务端响应：{}", rpcResponse.toString());
            /*
             * rpcResponse放入AttributeKey的过程，呼应NettyClient中sendRequest方法的
             * RpcResponse rpcResponse = channel.attr(key).get();
             * */
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
            ctx.channel().attr(key).set(rpcResponse);
            ctx.channel().close();
        } finally {
            ReferenceCountUtil.release(rpcResponse);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("[NettyClientHandler]：客户端处理响应过程中发生错误");
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }

}
