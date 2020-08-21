package com.chuang.rpc.socket.server;

import com.chuang.rpc.enumeration.RPCError;
import com.chuang.rpc.exception.RPCException;
import com.chuang.rpc.hook.ShutdownHook;
import com.chuang.rpc.interfaces.RpcServer;
import com.chuang.rpc.provider.DefaultServiceProvider;
import com.chuang.rpc.provider.ServiceProvider;
import com.chuang.rpc.registry.NacosServiceRegistry;
import com.chuang.rpc.registry.ServiceRegistry;
import com.chuang.rpc.serializer.Serializer;
import com.chuang.rpc.server.RequestHandler;
import com.chuang.rpc.server.RequestHandlerThread;
import com.chuang.rpc.server.ThreadWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.*;

/**
 * 以Socket方式实现的RpcServer
 * 服务端类，监听某端口，循环接收连接请求，如果收到请求就创建一个线程，并调用相关方法处理请求
 * */
public class SocketServer implements RpcServer {
    private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);

    // 先固定线程池参数，推荐以静态final变量形式确定
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 20;
    private static final int KEEP_ALIVE_TIME = 60;
    private static final int BLOCKING_QUEUE_CAPACITY = 100;

    // 本服务端的host和port，用于经Nacos注册中心传递给将调用本服务端的客户端
    // 也因为作为服务端，不清楚何时会有怎样的客户端访问，因此，将host和port固化，有利于减少复杂度
    private final String host;
    private final int port;
    // V3.0：因为需要将当前服务端及所含服务注册到中心，增加serviceRegistry，serviceProvider则用于保存当前所含服务
    private final ServiceRegistry serviceRegistry;

    // 利用线程池平衡生产和消费数量
    private final ExecutorService threadPool;
    // 用serviceProvider对象专门负责服务注册
    private final ServiceProvider serviceProvider;
    private Serializer serializer;

    public SocketServer(String host, int port){
        this.host = host;
        this.port = port;
        this.serviceProvider = new DefaultServiceProvider();
        this.serviceRegistry = new NacosServiceRegistry();

        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
        // 创建线程池
        threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                workQueue, Executors.defaultThreadFactory());
    }

    @Override
    public <T> void publishService(Object serviceObject, Class<?> serviceClass) {
        if(serializer == null){
            logger.error("未设置序列化器");
            throw new RPCException(RPCError.SERIALIZER_NOT_FOUND);
        }
        serviceProvider.addService(serviceObject);
        serviceRegistry.register(serviceClass.getCanonicalName(), new InetSocketAddress(host, port));
    }


    // 前期调用注册器对服务进行注册后，收到请求，创建相应线程，提交至线程池中，线程对所要求的目标服务进行搜索、处理
    @Override
    public void start(){
        try(ServerSocket serverSocket = new ServerSocket(port)){
            logger.info("服务器启动...");

            // 加钩子，实现服务端关闭前自动注销服务
            ShutdownHook.getShutdownHook().addClearAllHook(this);

            Socket socket;
            while ((socket = serverSocket.accept()) != null){
                logger.info("客户端连接成功！IP：{}:{}", socket.getInetAddress().getHostAddress(), socket.getPort());
                // 因使用注册器对象注册服务，因此在线程设计时，将创建线程和逻辑处理分开
                // RequestHandlerThread是创建出的线程，从serviceProvider处获得服务对象，将Request对象和服务对象交给RequestHandler
                // RequestHandler处理请求，调用目标方法，并返回结果，反射等过程也放到此处
                threadPool.execute(new RequestHandlerThread(socket, new RequestHandler(), serviceProvider, serializer));
            }
            threadPool.shutdown();
        }catch (IOException e){
            logger.error("服务器启动/注册服务时发生错误：", e);
        }
    }

    @Override
    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    /*------------不再需要RpcServe对象实现注册，该方法删除------------*/
    // 提供对port的注册功能，持续监视port端口，有输入后，调用service服务执行请求对象中的指定方法
    public void register(Object service, int port){
        try(ServerSocket serverSocket = new ServerSocket(port)){
            logger.info("服务器正在启动...");
            Socket socket;
            while((socket = serverSocket.accept()) != null){
                logger.info("与客户端连接成功！IP为：{}", socket.getInetAddress().getHostAddress());
                // 将工作内容作为ThreadWorker对象封装，并提交至线程池
                threadPool.execute(new ThreadWorker(socket, service));
            }

        }catch (IOException e){
            logger.error("连接时发生错误：", e);
        }
    }

    @Override
    public InetSocketAddress getAddress() {
        return new InetSocketAddress(host, port);
    }

    @Override
    public Set<String> getAllServices() {
        return serviceProvider.getAllServices();
    }
}