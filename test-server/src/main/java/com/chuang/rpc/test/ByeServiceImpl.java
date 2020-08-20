package com.chuang.rpc.test;

import com.chuang.rpc.api.ByeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByeServiceImpl implements ByeService {
    private static final Logger logger = LoggerFactory.getLogger(ByeServiceImpl.class);
    @Override
    public String bye() {
        logger.info("接收到分手请求");
        return "那就这样吧，bye~";
    }
}
