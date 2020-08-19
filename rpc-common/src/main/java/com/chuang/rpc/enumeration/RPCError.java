package com.chuang.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误类型，用于
 *      服务对象注册
 *      序列化
 * */
@AllArgsConstructor
@Getter
public enum RPCError {

    SERVICE_INVOCATION_FAILURE("服务调用出现失败"),
    SERVICE_CAN_NOT_BE_NULL("注册服务不能为空"),
    SERVICE_NOT_FOUND("找不到对应的服务"),
    SERVICE_NOT_IMPLEMENT_ANT_INTERFACE("注册的服务未实现已有接口"),

    UNKNOWN_PROTOCOL("无法识别的数据包协议"),
    UNKNOWN_SERIALIZER("无法识别的（反）序列化器类型"),
    UNKNOWN_PACKAGE_TYPE("无法识别的数据包类型"),

    SERIALIZER_NOT_FOUND("未设置序列化器"),
    RESPONSE_NOT_MATCH("响应与请求号不匹配"),

    FAILED_TO_CONNECT_TO_SERVICE_REGISTRY("连接注册中心失败"),
    REGISTER_SERVICE_FAILED("注册服务失败"),
    CLIENT_CONNECT_SERVER_FAILED("客户端连接服务端失败");

    private final String message;
}
