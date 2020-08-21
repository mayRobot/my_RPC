package com.chuang.rpc.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/*
* 负载平衡：用于注册中心lookupService选择提供服务的服务端时
* */
public interface LoadBalancer {
    Instance slectInstance(List<Instance> instances);
}
