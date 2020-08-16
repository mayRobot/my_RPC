package com.chuang.rpc.interfaces;

import com.chuang.rpc.entity.RpcRequest;

/**
 * 客户端类，将接收到的Request请求发送并返回接收到的结果
 * */
public interface RpcClient {
    // 发送Request，并返回之后得到的结果
    Object sendRequest(RpcRequest rpcRequest);
}
