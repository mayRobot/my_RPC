package com.chuang.rpc.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 消费者（客户端）发送请求时的请求对象，包含请求的各种信息，使生产者（服务端）能唯一确定所要调用的方法
 * 方法名和参数类型能确定唯一方法，加上所传参数，就可以唯一确定所需调用方法
 * */
@Data
@Builder
public class RpcRequest implements Serializable {
    //待调用对象的接口名称
    private String interfaceName;
    //待调用方法名称
    private String methodName;
    //调用的方法的形参参数类型
    private Class<?>[] paramsTypes;
    //方法参数
    private Object[] params;


}
