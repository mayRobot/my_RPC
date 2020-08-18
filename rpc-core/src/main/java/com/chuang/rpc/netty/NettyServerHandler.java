package com.chuang.rpc.netty;

import com.chuang.rpc.entity.RpcRequest;
import com.chuang.rpc.entity.RpcResponse;
import com.chuang.rpc.provider.DefaultServiceProvider;
import com.chuang.rpc.provider.ServiceProvider;
import com.chuang.rpc.server.RequestHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端业务处理类，扩展SimpleChannelInboundHandler
 *
 * 处于责任链尾部，无需关心编解码、序列化的问题，直接处理请求对象rpcRequest
 * 因为要处理发送的rpcRequest，因此限定为RpcRequest
 * 接收rpcRequest，读取其中各项信息，锁定目标服务对象和方法，并进行调用，将结果作为response返回
 * 通过服务注册表serviceRegistry获取服务对象，处理实施交由requestHandler
 * */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private static ServiceProvider serviceProvider;
    private static RequestHandler requestHandler;

    static {
        requestHandler = new RequestHandler();
        serviceProvider = new DefaultServiceProvider();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        try{
            logger.info("[NettyServerHandler]：服务器开始对请求 {} 使用目标服务对象调用目标方法", rpcRequest);
            // 获取目标服务对象接口，用于寻找服务对象
            String interfaceName = rpcRequest.getInterfaceName();
            Object serviceObject = serviceProvider.getService(interfaceName);
            // 交由RequestHandler处理
            Object result = requestHandler.handle(rpcRequest, serviceObject);

            /*------------------------- 上述过程同socket方式，后面开始不一样------------*/
            // 将response输入到管道中准备发送
            ChannelFuture future = ctx.writeAndFlush(RpcResponse.success(result));
            future.addListener(ChannelFutureListener.CLOSE);//TODO 啥意思
        }finally {
            ReferenceCountUtil.release(rpcRequest);//TODO 啥意思 释放rpcRequest，防止占用资源？
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("[NettyServerHandler]：服务端处理请求过程中发生错误");
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }

}
