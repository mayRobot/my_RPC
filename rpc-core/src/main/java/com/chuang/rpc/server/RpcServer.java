package com.chuang.rpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * 服务端类，监听某端口，循环接收连接请求，如果收到请求就创建一个线程，并调用相关方法处理请求
 * */
public class RpcServer {
    // 利用线程池平衡生产和消费数量
    private final ExecutorService threadPool;
    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    public RpcServer(){
        // 先固定线程池参数
        int corePoolSize = 5;
        int maximumPoolSize = 20;
        long keepAliveTime = 60;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(100);
        // 创建线程池
        threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS,
                workQueue, Executors.defaultThreadFactory());
    }

    // 提供对port的注册功能，持续监视port端口，有输入后，调用service服务执行请求对象中的指定方法
    public void register(Object service, int port){
        try(ServerSocket serverSocket = new ServerSocket(port)){
            logger.info("服务器正在启动...");
            Socket socket;
            while((socket = serverSocket.accept()) != null){
                logger.info("与客户端连接成功！IP为：{}", socket.getInetAddress().getHostAddress());
                // 将工作内容作为WorkerThread对象封装，并提交至线程池
                threadPool.execute(new ThreadWorker(socket, service));
            }

        }catch (IOException e){
            logger.error("连接时发生错误：", e);
        }
    }
}
