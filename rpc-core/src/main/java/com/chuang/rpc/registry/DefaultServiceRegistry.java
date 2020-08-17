package com.chuang.rpc.registry;

import com.chuang.rpc.enumeration.RPCError;
import com.chuang.rpc.exception.RPCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的注册类，提供服务对象注册、对应接口查询等功能
 * */
public class DefaultServiceRegistry implements ServiceRegistry{

    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceRegistry.class);

    // 将服务名和执行服务的对象放于线程安全的concurrentHashMap中，并使用set保存当前已被注册的对象，服务名默认为对象实现的接口名
    // 默认将对象实现的服务接口作为服务名，若某对象实现多个接口，则两个服务名对应同一个对象，同一时间，某个接口只能有一个对象提供服务
    private static final Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    private static final Set<String> registeredService = ConcurrentHashMap.newKeySet();

    @Override
    public synchronized <T> void register(T service) {
        /*
        * 对于大部分class而言，getCanonicalName和getName这两个方法没有什么不同的， 但是对于array等就显示出来了。
        * getName()方法，以String的形式，返回Class对象的‘实体’名称；
        * getSimpleName()方法，是获取源代码中给出的‘底层类’简称
        * https://www.cnblogs.com/birkhoff/p/5274562.html
        * */
        String serviceName = service.getClass().getCanonicalName();
        // 若已经注册了该对象，直接return
        if(registeredService.contains(serviceName)) return;
        registeredService.add(serviceName);
        Class<?>[] interfaces = service.getClass().getInterfaces();
        // 若没有接口，则抛出未实现接口错误
        if(interfaces.length == 0){
            throw new RPCException(RPCError.SERVICE_NOT_IMPLEMENT_ANT_INTERFACE);
        }
        // 将所有接口对应的服务对象设为service
        // 若某服务还未结束，同一个接口的实现服务对象进入，就直接抛弃旧服务？？？
        for(Class<?> i : interfaces)
            serviceMap.put(i.getCanonicalName(), service);
        logger.info("注册服务：{} 实现接口：{}", serviceName, interfaces);
    }

    @Override
    public synchronized Object getService(String serviceName) {
        // 服务名默认是服务对象实现的接口名
        Object serviceObject = serviceMap.get(serviceName);
        if(serviceObject == null)
            throw new RPCException(RPCError.SERVICE_NOT_FOUND);
        return serviceObject;
    }
}
