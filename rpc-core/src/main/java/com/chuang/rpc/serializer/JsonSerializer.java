package com.chuang.rpc.serializer;

import com.chuang.rpc.entity.RpcRequest;
import com.chuang.rpc.enumeration.SerializerCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Json格式数据的序列化器
 * */
public class JsonSerializer implements Serializer {

    private static final Logger logger = LoggerFactory.getLogger(JsonSerializer.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(Object object) {
        try{
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            logger.error("序列化时错误：{}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try{
            Object object = objectMapper.readValue(bytes, clazz);
            if(object instanceof RpcRequest){
                object = handleRequest(object);
            }
            return object;
        } catch (IOException e) {
            logger.error("反序列化时发生错误：{}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /*
        这里由于使用JSON序列化和反序列化Object数组，无法保证反序列化后仍然为原实例类型
        需要重新判断处理
     */
    private Object handleRequest(Object object) throws IOException{
        logger.info("handleRequest start work");
        RpcRequest rpcRequest = (RpcRequest) object;
        for(int i = 0; i < rpcRequest.getParamsTypes().length; i++) {
            Class<?> clazz = rpcRequest.getParamsTypes()[i];
            if(!clazz.isAssignableFrom(rpcRequest.getParams()[i].getClass())) {
                byte[] bytes = objectMapper.writeValueAsBytes(rpcRequest.getParams()[i]);
                rpcRequest.getParams()[i] = objectMapper.readValue(bytes, clazz);
            }
        }
        return rpcRequest;
    }

    @Override
    public int getCode() {
        return SerializerCode.valueOf("JSON").getCode();
    }
}
