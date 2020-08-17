package com.chuang.rpc.client;

import com.chuang.rpc.entity.RpcRequest;
import com.chuang.rpc.entity.RpcResponse;
import com.chuang.rpc.interfaces.RpcClient;
import com.chuang.rpc.socket.client.SocketClient;
import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 动态代理生成客户端实例对象，并生成相应的RpcRequest对象发送给服务端，并获取返回的数据
 * */
@AllArgsConstructor
public class RpcClientProxy implements InvocationHandler {
    // host、port在rpcClient中已经定义
    private RpcClient rpcClient;

    // 生成代理对象
    //https://www.cnblogs.com/unknows/p/10261292.html 忽略unchecked警告
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> tClass){
        return (T) Proxy.newProxyInstance(tClass.getClassLoader(), new Class<?>[] {tClass}, this);
    }

    // 创建RpcRequest对象，由RpcClient发送，返回RpcResponse的Data对象
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 使用RpcRequest的建造者模式
//        RpcRequest rpcRequest = RpcRequest.builder().
//                interfaceName(method.getDeclaringClass().getName()).
//                methodName(method.getName()).
//                paramsTypes(method.getParameterTypes()).
//                params(args).
//                build();

        RpcRequest rpcRequest = new RpcRequest(method.getDeclaringClass().getName(), method.getName()
        , method.getParameterTypes(), args);

        // V2.0开始，sendRequest返回response的data部分
        return rpcClient.sendRequest(rpcRequest);
    }
}
