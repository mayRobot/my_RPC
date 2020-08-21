package com.chuang.rpc.registry;


import com.chuang.rpc.loadbalancer.LoadBalancer;
import java.net.InetSocketAddress;

/**
 * 服务注册中心
 * 将服务名及可提供该服务的服务端地址进行注册，并可通过服务名获得可提供服务的服务端地址，实现动态分配服务端
 * */
public interface ServiceRegistry {
    // 将服务名、服务提供者地址注册进注册中心
    void register(String serviceName, InetSocketAddress inetSocketAddress);
    // 获取可提供某服务的服务端地址（加入负载均衡后，可以直接返回最恰当的服务端地址）
    InetSocketAddress lookupService(String serviceName);
    InetSocketAddress lookupService(String serviceName, LoadBalancer loadBalancer);
}
