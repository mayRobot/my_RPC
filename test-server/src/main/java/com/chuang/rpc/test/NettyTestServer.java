package com.chuang.rpc.test;

import com.chuang.rpc.api.ByeService;
import com.chuang.rpc.api.HelloService;
import com.chuang.rpc.interfaces.RpcServer;
import com.chuang.rpc.netty.NettyServer;
import com.chuang.rpc.provider.DefaultServiceProvider;
import com.chuang.rpc.provider.ServiceProvider;
import com.chuang.rpc.serializer.JsonSerializer;
import com.chuang.rpc.serializer.KryoSerializer;

public class NettyTestServer {
    public static void main(String[] args) {
        new Thread("helloServer"){
            @Override
            public void run() {
                //V3.0：服务对象注册放于Server内部，无需另建
                NettyServer helloServer = new NettyServer("172.16.9.40", 9999);
                helloServer.setSerializer(new KryoSerializer());

                HelloService helloService = new HelloServiceImpl();
                // 注册服务
                helloServer.publishService(helloService, HelloService.class);

                helloServer.start();
            }
        }.start();

        // 再建一个服务端，用于处理ByeService
        new Thread("byeServer"){
            @Override
            public void run() {
                NettyServer byeServer = new NettyServer("172.16.9.40", 9000);
                byeServer.setSerializer(new JsonSerializer());

                ByeService byeService = new ByeServiceImpl();
                byeServer.publishService(byeService, ByeService.class);

                byeServer.start();
            }
        }.start();


    }
}
