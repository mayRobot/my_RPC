//package com.chuang.rpc.utils;
//
//import java.util.concurrent.ExecutorService;
//
///**
// * 线程池工具类，用于优化代码结构
// * 使用者可根据静态方法获得线程池
// * */
//public class ThreadPoolFactory {
//
//    // 先固定线程池参数，推荐以静态final变量形式确定
//    private static final int CORE_POOL_SIZE = 5;
//    private static final int MAX_POOL_SIZE = 20;
//    private static final int KEEP_ALIVE_TIME = 60;
//    private static final int BLOCKING_QUEUE_CAPACITY = 100;
//
//    // 使用者只是使用，无需构造新的线程池，所以用private形式创建构造器
//    private ThreadPoolFactory(){}
//
//    public static ExecutorService createDefaultThreadPool(String threadNamePrefix){
//        return createDefaultThreadPool(threadNamePrefix, false);
//    }
//
//    //
//    public static ExecutorService createDefaultThreadPool(String threadNamePrefix, Boolean daemon){
//
//    }
//
//}
