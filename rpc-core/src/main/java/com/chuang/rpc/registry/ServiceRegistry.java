package com.chuang.rpc.registry;


/**
 * 服务注册接口，用于支持多种服务在服务器端的注册
 * */
public interface ServiceRegistry {
    // 注册服务信息
    <T> void register(T service);
    // 获取执行服务的服务对象
    Object getService(String serviceName);
}
