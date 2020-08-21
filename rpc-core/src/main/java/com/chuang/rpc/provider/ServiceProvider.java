package com.chuang.rpc.provider;

import java.util.Set;

/**
 * 将服务存储（注册）在此，为客户端提供服务
 * V3.0后，服务端暴露出来供使用的服务接口，支持多种服务在服务器端的注册，即之前的ServiceRegistry
 * */
public interface ServiceProvider {
    // 注册服务信息
    <T> void addService(T service);
    // 获取执行服务的服务对象
    Object getService(String serviceName);
    // 获取所有服务名
    Set<String> getAllServices();
}
