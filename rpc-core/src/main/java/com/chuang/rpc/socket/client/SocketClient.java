package com.chuang.rpc.socket.client;

import com.chuang.rpc.entity.RpcRequest;
import com.chuang.rpc.entity.RpcResponse;
import com.chuang.rpc.enumeration.RPCError;
import com.chuang.rpc.enumeration.ResponseCode;
import com.chuang.rpc.exception.RPCException;
import com.chuang.rpc.interfaces.RpcClient;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * socket方式实现的RpcClient
 * 客户端类，将接收到的Request请求发送并返回接收到的结果
 * */
@AllArgsConstructor
public class SocketClient implements RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);

    private final String host;
    private final int port;

    /* 发送请求，并获取返回的输入流
     * 使用Java的序列化方式，通过Socket传输。创建一个Socket，获取ObjectOutputStream对象，然后把需要发送的对象传进去即可，
     * 接收时获取ObjectInputStream对象，readObject()方法就可以获得一个返回的对象
     */
    @Override
    public Object sendRequest(RpcRequest rpcRequest){
        try(Socket socket = new Socket(host, port)){
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            // 将RpcRequest请求对象作为object参数传入socket输出流中，供服务端接收并通过readObject方法读取
            objectOutputStream.writeObject(rpcRequest);
            objectOutputStream.flush();
            // 2.0后，对response对象状态进行检查
            RpcResponse rpcResponse = (RpcResponse)objectInputStream.readObject();
            if(rpcResponse == null) {
                logger.error("服务调用失败，service：{}", rpcRequest.getInterfaceName());
                throw new RPCException(RPCError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }
            if(rpcResponse.getStatusCode() == null || rpcResponse.getStatusCode() != ResponseCode.SUCCESS.getCode()) {
                logger.error("调用服务失败, service: {}, response:{}", rpcRequest.getInterfaceName(), rpcResponse);
                throw new RPCException(RPCError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }
            //return rpcResponse;
            return rpcResponse.getData();
        }catch (IOException | ClassNotFoundException e){
            logger.error("调用时发生错误：", e);
            return null;
        }
    }
}