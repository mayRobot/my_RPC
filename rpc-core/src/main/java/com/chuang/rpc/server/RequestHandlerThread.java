package com.chuang.rpc.server;

import com.chuang.rpc.entity.RpcRequest;
import com.chuang.rpc.entity.RpcResponse;
import com.chuang.rpc.registry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 工作线程，取代ThreadWorker
 * RequestHandlerThread是创建出的线程，从ServicRegistry处获得服务对象，将Request对象和服务对象交给RequestHandler处理
 * 获得处理后的结果，并将其返回
 *
 * */
public class RequestHandlerThread implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(RequestHandlerThread.class);

    private Socket socket;
    private RequestHandler requestHandler;
    private ServiceRegistry serviceRegistry;

    public RequestHandlerThread(Socket socket, RequestHandler requestHandler, ServiceRegistry serviceRegistry) {
        this.socket = socket;
        this.requestHandler = requestHandler;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void run() {
        try(ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())){
            // 从socket中获取Request对象
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            // 获取请求对象中所需的服务对象对应的接口
            String interfaceName = rpcRequest.getInterfaceName();
            Object serviceObject = serviceRegistry.getService(interfaceName);
            // 调用RequestHandler完成对目标方法的调用
            Object result = requestHandler.handle(rpcRequest, serviceObject);

            // 将结果放于输出流中
            objectOutputStream.writeObject(RpcResponse.success(result));
            objectOutputStream.flush();

        }catch (IOException | ClassNotFoundException e){
            logger.error("查找目标方法或发送结果时发生错误：", e);
        }
    }
}
