package com.chuang.rpc.test;

import com.chuang.rpc.api.ByeService;
import com.chuang.rpc.api.HelloObject;
import com.chuang.rpc.api.HelloService;
import com.chuang.rpc.client.RpcClientProxy;
import com.chuang.rpc.interfaces.RpcClient;
import com.chuang.rpc.netty.NettyClient;
import com.chuang.rpc.serializer.JsonSerializer;
import com.chuang.rpc.serializer.KryoSerializer;

public class NettyTestClient {
    public static void main(String[] args) {
        // V3.0后，client无需设置host port，其他过程不变
        RpcClient rpcClient = new NettyClient();
        // V2.1后 必须显式设置序列化器
        rpcClient.setSerializer(new KryoSerializer());
        // 动态代理
        RpcClientProxy proxy = new RpcClientProxy(rpcClient);

        // 构造所要调用的服务对象接口，不同于服务端必须要执行，所以需要对应接口实现类实例作为服务对象
        HelloService service = proxy.getProxy(HelloService.class);
        HelloObject object = new HelloObject(88, "This a Netty message");

        String responseWord = service.hello(object);
        System.out.println(responseWord);

        ByeService byeService = proxy.getProxy(ByeService.class);

        responseWord = byeService.bye();
        System.out.println(responseWord);
    }
}
