package com.chuang.test;

import com.chuang.rpc.api.HelloService;
import com.chuang.rpc.server.RpcServer;

/**
 * 测试服务端，创建RpcServer客户端对象，通过register方法监听指定窗口，当收到请求后返回HelloServiceImpl对象
 * */
public class TestServer {
    public static void main(String[] args) {
        RpcServer rpcServer = new RpcServer();
        HelloService helloService = new HelloServiceImpl();

        // 重复查询9000端口，收到输入后即执行helloService中由请求对象指定的方法
        rpcServer.register(helloService, 9000);
    }
}
