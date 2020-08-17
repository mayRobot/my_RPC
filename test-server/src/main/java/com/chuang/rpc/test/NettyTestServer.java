package com.chuang.rpc.test;

import com.chuang.rpc.api.HelloService;
import com.chuang.rpc.interfaces.RpcServer;
import com.chuang.rpc.netty.NettyServer;
import com.chuang.rpc.registry.DefaultServiceRegistry;
import com.chuang.rpc.registry.ServiceRegistry;

public class NettyTestServer {
    public static void main(String[] args) {
        ServiceRegistry registry = new DefaultServiceRegistry();
        HelloService helloService = new HelloServiceImpl();
        registry.register(helloService);

        RpcServer server = new NettyServer();
        server.start(9999);
    }
}
