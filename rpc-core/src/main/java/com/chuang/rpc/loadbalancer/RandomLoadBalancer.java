package com.chuang.rpc.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.Random;

/*
* 随机
* */
public class RandomLoadBalancer implements LoadBalancer{
    @Override
    public Instance slectInstance(List<Instance> instances) {
        return instances.get(new Random().nextInt(instances.size()));
    }
}
