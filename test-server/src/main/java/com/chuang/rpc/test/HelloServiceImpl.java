package com.chuang.rpc.test;

import com.chuang.rpc.api.HelloObject;
import com.chuang.rpc.api.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务端实现类，实现rpc-api中的HelloService接口
 * */
public class HelloServiceImpl implements HelloService {
    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public String hello(HelloObject object) {
        logger.info("接收到：{}", object.getMessage());
        return "HelloService调用的返回值id：" + object.getId();
    }
}
