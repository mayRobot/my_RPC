package com.chuang.rpc.test;

import com.chuang.rpc.api.HelloObject;
import com.chuang.rpc.api.HelloService;
import com.chuang.rpc.client.RpcClientProxy;
import com.chuang.rpc.interfaces.RpcClient;
import com.chuang.rpc.netty.NettyClient;
import com.chuang.rpc.serializer.JsonSerializer;
import com.chuang.rpc.serializer.KryoSerializer;
import com.chuang.rpc.socket.client.SocketClient;

/*
* 并发式调用client
* */
public class ConcurrentTestClient {

    public static void main(String[] args) {
        new Thread("t1"){
            @Override
            public void run() {
                RpcClient client = new SocketClient();
                client.setSerializer(new JsonSerializer());
                RpcClientProxy proxy = new RpcClientProxy(client);

                HelloService service = proxy.getProxy(HelloService.class);
                HelloObject helloObject = new HelloObject(20, "This is t1");
                String response = service.hello(helloObject);

                System.out.println(response);

            }
        }.start();

        new Thread("t2"){
            @Override
            public void run() {
                RpcClient client = new SocketClient();
                client.setSerializer(new KryoSerializer());
                RpcClientProxy proxy = new RpcClientProxy(client);

                HelloService service = proxy.getProxy(HelloService.class);
                HelloObject helloObject = new HelloObject(30, "This is t2");
                String response = service.hello(helloObject);
                System.out.println(response);
            }
        }.start();

    }
}
