package com.chuang.rpc.server;

import com.chuang.rpc.entity.RpcRequest;
import com.chuang.rpc.entity.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * 封装socket和service，用于线程池任务提交
 * run方法中负责获取Request并将Response封装输出
 * */
public class ThreadWorker implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ThreadWorker.class);
    // socket保存传入请求，可以还原出所需调用的方法及参数
    private Socket socket;
    // 调用方法的实例对象
    private Object service;

    public ThreadWorker(Socket socket, Object service) {
        this.socket = socket;
        this.service = service;
    }

    @Override
    public void run() {
        try(
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(this.socket.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(this.socket.getInputStream())
        ){
            // 从socket输入流中还原出请求对象
            RpcRequest rpcRequest = (RpcRequest)objectInputStream.readObject();
            // 利用request中的方法等信息唯一确定所需调用方法
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsTypes());
            // method.invoke(obj, args) 等同于 obj.method(args)
            Object returnObjct = method.invoke(service, rpcRequest.getParams());
            objectOutputStream.writeObject(RpcResponse.success(returnObjct));
            objectOutputStream.flush();
        }catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            logger.error("任务处理中发生错误：", e);
        }
    }
}
