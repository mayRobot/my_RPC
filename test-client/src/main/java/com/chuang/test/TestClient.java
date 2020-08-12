package com.chuang.test;

import com.chuang.rpc.api.HelloObject;
import com.chuang.rpc.api.HelloService;
import com.chuang.rpc.client.RpcClientProxy;

/**
 * 测试客户端：通过动态代理，创建hello服务对象，并创建helloObject存储发送的信息，通过hello服务发送
 * 实际上，过程中会调用invoke方法，将信息封装、发送给服务端，并由服务端接收请求对象中记录的方法名--hello，调用方法并封装为返回值返回
 * 即hello方法在客户端并不执行，仅仅起到告知服务端需要执行哪个方法的作用
 * */
public class TestClient {
    public static void main(String[] args) {
        RpcClientProxy proxy = new RpcClientProxy("172.16.9.146", 9000);
        HelloService helloService = proxy.getProxy(HelloService.class);
        HelloObject helloObject = new HelloObject(55, "This is a message");

        // hello方法在客户端并不执行，仅仅起到告知服务端需要执行哪个方法的作用
        String response = helloService.hello(helloObject);
        System.out.println(response);
    }

}
