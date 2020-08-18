package com.chuang.rpc.test;

import com.chuang.rpc.api.HelloService;
import com.chuang.rpc.interfaces.RpcServer;
import com.chuang.rpc.provider.DefaultServiceProvider;
import com.chuang.rpc.provider.ServiceProvider;
import com.chuang.rpc.registry.ServiceRegistry;
import com.chuang.rpc.serializer.JsonSerializer;
import com.chuang.rpc.socket.server.SocketServer;

/**
 * 测试服务端，创建RpcServer客户端对象，serviceRegistry负责注册服务
 * 通过start方法监听指定窗口，当收到请求后返回HelloServiceImpl对象
 * */
public class TestServer {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        // 由serviceRegistry负责注册服务
        ServiceProvider serviceProvider = new DefaultServiceProvider();
        serviceProvider.register(helloService);

        RpcServer rpcServer = new SocketServer(serviceProvider);
        rpcServer.setSerializer(new JsonSerializer());
        rpcServer.start(9999);

//        RpcServer rpcServer = new RpcServer();
//        HelloService helloService = new HelloServiceImpl();
//
//        // 重复查询9000端口，收到输入后即执行helloService中由请求对象指定的方法
//        rpcServer.register(helloService, 9000);
    }
}
