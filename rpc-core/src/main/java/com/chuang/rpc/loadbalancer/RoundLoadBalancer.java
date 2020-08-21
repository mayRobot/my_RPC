package com.chuang.rpc.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/*
* 转轮法：用变量记录之前选择的第几个，然后本次选择下一个
* */
public class RoundLoadBalancer implements LoadBalancer{
    private int index = 0;

    @Override
    public Instance slectInstance(List<Instance> instances) {
        index %= instances.size();
        return instances.get(index++);
    }
}
