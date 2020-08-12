package com.chuang.rpc.entity;

import com.chuang.rpc.enumeration.ResponseCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 服务端执行结束后，将所得信息包装为返回对象，传输给客户端
 * */
@Data
public class RpcResponse<T> implements Serializable {
    //响应状态码
    private Integer statusCode;
    //状态说明信息
    private String message;
    //响应数据
    private T data;


    //快速生成成功和失败的响应对象
    public static <T> RpcResponse<T> success(T data){
        RpcResponse<T> response = new RpcResponse<>();
        response.setStatusCode(ResponseCode.SUCCESS.getCode());
        response.setMessage(ResponseCode.SUCCESS.getMessage());
        response.setData(data);
        return response;
    }

    public static <T> RpcResponse<T> fail(ResponseCode code){
        RpcResponse<T> response = new RpcResponse<>();
        response.setStatusCode(code.getCode());
        response.setMessage(code.getMessage());
        return response;
    }

}
