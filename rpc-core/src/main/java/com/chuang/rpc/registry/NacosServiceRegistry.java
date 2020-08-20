package com.chuang.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.chuang.rpc.enumeration.RPCError;
import com.chuang.rpc.exception.RPCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 使用Nacos作为注册中心实现类
 * 将服务名及可提供该服务的服务端地址进行注册，并可通过服务名获得可提供服务的服务端地址，实现动态分配服务端
 * 先将nacos安装在电脑上，然后以单机模式运行sh startup.sh -m standalone
 * */
public class NacosServiceRegistry implements ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(NacosServiceRegistry.class);

    private static final String SERVER_ADDR = "192.168.249.1:8848";
    //namingService 提供了两个很方便的接口，registerInstance 和 getAllInstances 方法，
    // 前者可以直接向 Nacos 注册服务，后者可以获得提供某个服务的所有提供者的列表。
    // 设置为static，所有该类实例均可获取已注册的服务端信息
    private static final NamingService namingService;

    //通过 NamingFactory 创建 NamingService 连接 Nacos，连接的过程写在了静态代码块中，在类加载时自动连接
    static {
        try{
            namingService = NamingFactory.createNamingService(SERVER_ADDR);
        } catch (NacosException e) {
            logger.error("连接Nacos失败：", e);
            throw new RPCException(RPCError.FAILED_TO_CONNECT_TO_SERVICE_REGISTRY);
        }
    }

    // 注册服务
    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        try{
            //TODO getHostString应该也可以，随后可做尝试
            namingService.registerInstance(serviceName, inetSocketAddress.getHostName(), inetSocketAddress.getPort());
        } catch (NacosException e) {
            logger.error("注册服务时发生错误：", e);
            throw new RPCException(RPCError.REGISTER_SERVICE_FAILED);
        }
    }

    // 查找提供serviceName对应服务的所有服务端
    @Override
    public InetSocketAddress lookupService(String serviceName) {
        try{
            List<Instance> instances = namingService.getAllInstances(serviceName);
            /*
            * 通过 getAllInstance 获取到某个服务的所有提供者列表后，需要选择一个，这里涉及了负载均衡策略，暂时先选择第 0 个
            * */
            Instance instance = instances.get(0);
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (NacosException e) {
            logger.error("获取服务时发生错误：", e);
            //throw new RPCException(RPCError.SERVICE_NOT_FOUND);
        }
        return null;
    }
}
