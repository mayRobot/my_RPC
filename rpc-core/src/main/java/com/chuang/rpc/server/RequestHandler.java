package com.chuang.rpc.server;

import com.chuang.rpc.entity.RpcRequest;
import com.chuang.rpc.entity.RpcResponse;
import com.chuang.rpc.enumeration.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * 对服务端接收的解析后请求进行处理，根据请求对象内容和指定的服务对象
 * 调用对应方法，获得结果后，返回该结果
 *
 * */
public class RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    /*
    * 通过请求对象信息获取目标方法信息，并在服务对象中找到相应方法并返回结果
    * */
    public Object handle(RpcRequest rpcRequest, Object serviceObject){
        Object result = null;
        try{
            result = invokeTargeMethod(rpcRequest, serviceObject);
            logger.info("服务 {} 成功调用方法：{}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        }catch (InvocationTargetException | IllegalAccessException e){
            logger.error("调用目标方法时发生错误: ", e);
        }
        return result;
    }

    /*
    * 调用目标方法
    * 专门实现目标方法的调用，若无法找到该方法，则返回相应状态码
    * */
    private Object invokeTargeMethod(RpcRequest rpcRequest, Object serviceObject) throws InvocationTargetException, IllegalAccessException {
        Method method;
        try{
            // 利用RpcRequest中信息，获取服务对象serviceObject中的指定类方法
            method = serviceObject.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsTypes());
        }catch (NoSuchMethodException e){
            return RpcResponse.fail(ResponseCode.NOT_FOUND_METHOD);
        }
        // 调用方法，method.invoke(obj, args) 等同于 obj.method(args)
        return method.invoke(serviceObject, rpcRequest.getParams());
    }

}
