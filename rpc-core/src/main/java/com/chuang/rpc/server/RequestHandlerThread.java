package com.chuang.rpc.server;

import com.chuang.rpc.entity.RpcRequest;
import com.chuang.rpc.entity.RpcResponse;
import com.chuang.rpc.provider.ServiceProvider;
import com.chuang.rpc.serializer.Serializer;
import com.chuang.rpc.socket.utils.ObjectReader;
import com.chuang.rpc.socket.utils.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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
    private ServiceProvider serviceProvider;
    private Serializer serializer;

    public RequestHandlerThread(Socket socket, RequestHandler requestHandler, ServiceProvider serviceProvider, Serializer serializer) {
        this.socket = socket;
        this.requestHandler = requestHandler;
        this.serviceProvider = serviceProvider;
        this.serializer = serializer;
    }

    // 使用工具类实现socket下也能选择序列化器
    @Override
    public void run() {
        try(InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream()){
            // 从socket中获取Request对象，V2.2开始使用ObjectOutput实现可选序列化器的socket传输
            RpcRequest rpcRequest = (RpcRequest) ObjectReader.readObject(inputStream);
            logger.info("解码器：{} 解码结束，请求已获取，", serializer.getClass());
            // 获取请求对象中所需的服务对象对应的接口
            String interfaceName = rpcRequest.getInterfaceName();
            Object serviceObject = serviceProvider.getService(interfaceName);
            // 调用RequestHandler完成对目标方法的调用
            Object result = requestHandler.handle(rpcRequest, serviceObject);

            // 将结果放于输出流中
            RpcResponse response = RpcResponse.success(result);
            ObjectWriter.writeObject(outputStream, response, serializer);
            outputStream.flush();

        }catch (IOException e){
            logger.error("查找目标方法或发送结果时发生错误：", e);
        }
    }
}
