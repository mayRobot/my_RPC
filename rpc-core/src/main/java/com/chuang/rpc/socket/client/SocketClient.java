package com.chuang.rpc.socket.client;

import com.chuang.rpc.entity.RpcRequest;
import com.chuang.rpc.entity.RpcResponse;
import com.chuang.rpc.enumeration.RPCError;
import com.chuang.rpc.enumeration.ResponseCode;
import com.chuang.rpc.exception.RPCException;
import com.chuang.rpc.interfaces.RpcClient;
import com.chuang.rpc.serializer.Serializer;
import com.chuang.rpc.socket.utils.ObjectReader;
import com.chuang.rpc.socket.utils.ObjectWriter;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

/**
 * socket方式实现的RpcClient
 * 客户端类，将接收到的Request请求发送并返回接收到的结果
 * */
public class SocketClient implements RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);

    private final String host;
    private final int port;

    private Serializer serializer;

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /* 发送请求，并获取返回的输入流
     * 使用Java的序列化方式，通过Socket传输。创建一个Socket，获取ObjectOutputStream对象，然后把需要发送的对象传进去即可，
     * 接收时获取ObjectInputStream对象，readObject()方法就可以获得一个返回的对象
     */
    @Override
    public Object sendRequest(RpcRequest rpcRequest){
        if(serializer == null){
            logger.error("未设置序列化器");
            throw new RPCException(RPCError.SERIALIZER_NOT_FOUND);
        }

        try(Socket socket = new Socket(host, port)){
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            // 将RpcRequest请求对象作为object参数传入socket输出流中，供服务端接收并通过readObject方法读取
            // V2.2：使用专门定义的ObjectWriter和Reader类，实现增加序列化器的socket传输
            ObjectWriter.writeObject(outputStream, rpcRequest, serializer);
            logger.info("编码器：{} 编码结束，已发送", serializer.getClass());
            // 2.0后，对response对象状态进行检查
            RpcResponse rpcResponse = (RpcResponse) ObjectReader.readObject(inputStream);
            if(rpcResponse == null) {
                logger.error("服务调用失败，service：{}", rpcRequest.getInterfaceName());
                throw new RPCException(RPCError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }
            if(rpcResponse.getStatusCode() == null || rpcResponse.getStatusCode() != ResponseCode.SUCCESS.getCode()) {
                logger.error("调用服务失败, service: {}, response:{}", rpcRequest.getInterfaceName(), rpcResponse);
                throw new RPCException(RPCError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }
            return rpcResponse.getData();
        }catch (IOException e){
            logger.error("调用时发生错误：", e);
            return null;
        }
    }

    @Override
    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }
}