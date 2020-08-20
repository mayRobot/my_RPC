package com.chuang.rpc.interfaces;

import com.chuang.rpc.serializer.Serializer;

/**
 * 服务端类，监听某端口，循环接收连接请求，如果收到请求就创建一个线程，并调用相关方法处理请求
 * 修改为服务端接口，可由socket和netty分别实现
 * */
public interface RpcServer {
    // V3.0增加，用于向Nacos注册服务，将本服务端及可提供的服务注册到Nacos中，服务采用服务对象形式，方便后续调用
    // 将服务注册放在server内部，先将各种服务注册结束，再调用start开始接收请求
    <T> void publishService(Object serviceObject, Class<?> serviceClass);

    // 前期调用注册器对服务进行注册后，收到请求，创建相应线程，提交至线程池中，线程对所要求的目标服务进行搜索、处理
    void start();
    // 增加设置序列化器选项
    void setSerializer(Serializer serializer);

}