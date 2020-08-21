package com.chuang.rpc.hook;

import com.chuang.rpc.interfaces.RpcServer;
import com.chuang.rpc.registry.NacosServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class ShutdownHook {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);

    ExecutorService threadPool;

    // 单例模式，通过private的构造器
    private static final ShutdownHook shutdownHook = new ShutdownHook();

    private ShutdownHook(){
        int CORE_POOL_SIZE = 5;
        int MAX_POOL_SIZE = 20;
        int KEEP_ALIVE_TIME = 60;
        int BLOCKING_QUEUE_CAPACITY = 100;

        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
        // 创建线程池
        threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                workQueue, Executors.defaultThreadFactory());
    }

    public static ShutdownHook getShutdownHook(){
        return shutdownHook;
    }

    //Runtime 对象是 JVM 虚拟机的运行时环境，
    // 调用其 addShutdownHook 方法增加一个钩子函数，创建一个新线程调用 clearRegistry 方法完成注销工作。
    // 这个钩子函数会在 JVM 关闭之前被调用
    public void addClearAllHook(RpcServer rpcServer){
        logger.info("关闭后将自动注销所有服务");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            NacosServiceRegistry.clearAllRegistry(rpcServer);
            threadPool.shutdown();
        }));
    }

}
