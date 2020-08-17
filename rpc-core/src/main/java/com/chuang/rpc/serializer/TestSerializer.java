package com.chuang.rpc.serializer;

import com.chuang.rpc.api.HelloObject;
import com.chuang.rpc.api.HelloService;
import com.chuang.rpc.entity.RpcRequest;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class TestSerializer {
    public static void main(String[] args) {
        //RpcRequest(interfaceName=com.chuang.rpc.api.HelloService, methodName=hello,
        // paramsTypes=[class com.chuang.rpc.api.HelloObject], params=[HelloObject(id=88, message=This a Netty message)])

        //TestSerializer testSerializer = new TestSerializer();
        Serializer serializer = new JsonSerializer();
        //HelloService helloService = testSerializer.getProxy(HelloService.class);

        HelloObject helloObject = new HelloObject(78, "This is a message");
        RpcRequest request = new RpcRequest("com.chuang.rpc.api.HelloService", "hello",
        new Class<?>[]{com.chuang.rpc.api.HelloObject.class}, new Object[]{helloObject});

        byte[] data = serializer.serialize(request);
        RpcRequest back = (RpcRequest)serializer.deserialize(data, request.getClass());
        System.out.println(back);
    }

}
