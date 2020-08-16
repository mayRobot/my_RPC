package com.chuang.rpc.interfaces;

/**
 * 服务端类，监听某端口，循环接收连接请求，如果收到请求就创建一个线程，并调用相关方法处理请求
 * 修改为服务端接口，可由socket和netty分别实现
 * */
public interface RpcServer {
    // 前期调用注册器对服务进行注册后，收到请求，创建相应线程，提交至线程池中，线程对所要求的目标服务进行搜索、处理
    void start(int port);
}