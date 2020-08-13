package com.chuang.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 服务对象注册中的错误类型
 * */
@AllArgsConstructor
@Getter
public enum RPCError {

    SERVICE_INVOCATION_FAILURE("服务调用出现失败"),
    SERVICE_CAN_NOT_BE_NULL("注册服务不能为空"),
    SERVICE_NOT_FOUND("找不到对应的服务"),
    SERVICE_NOT_IMPLEMENT_ANT_INTERFACE("注册的服务未实现已有接口");

    private final String message;
}
