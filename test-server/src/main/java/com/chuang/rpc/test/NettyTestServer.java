package com.chuang.rpc.test;

import com.chuang.rpc.api.HelloService;
import com.chuang.rpc.interfaces.RpcServer;
import com.chuang.rpc.netty.NettyServer;
import com.chuang.rpc.provider.DefaultServiceProvider;
import com.chuang.rpc.provider.ServiceProvider;
import com.chuang.rpc.registry.ServiceRegistry;
import com.chuang.rpc.serializer.JsonSerializer;

public class NettyTestServer {
    public static void main(String[] args) {
        ServiceProvider provider = new DefaultServiceProvider();
        HelloService helloService = new HelloServiceImpl();
        provider.register(helloService);

        RpcServer server = new NettyServer();
        server.setSerializer(new JsonSerializer());
        server.start(9999);
    }
}
