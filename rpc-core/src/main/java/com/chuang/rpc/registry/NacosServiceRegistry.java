package com.chuang.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.chuang.rpc.enumeration.RPCError;
import com.chuang.rpc.exception.RPCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * 使用Nacos作为注册中心实现类
 * 将服务名及可提供该服务的服务端地址进行注册，并可通过服务名获得可提供服务的服务端地址，实现动态分配服务端
 * */
public class NacosServiceRegistry implements ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(NacosServiceRegistry.class);

    private static final String SERVER_ADDR = "ip:port";
    private static final NamingService namingService;

    static {
        try{
            namingService = NamingFactory.createNamingService(SERVER_ADDR);
        } catch (NacosException e) {
            logger.error("连接Nacos失败：", e);
            throw new RPCException(RPCError.FAILED_TO_CONNECT_TO_SERVICE_REGISTRY);
        }
    }

    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {

    }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        return null;
    }
}
