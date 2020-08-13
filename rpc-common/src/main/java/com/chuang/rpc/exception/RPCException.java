package com.chuang.rpc.exception;

import com.chuang.rpc.enumeration.RPCError;

/**
 * RPC异常，目前用于服务对象注册
 * */
public class RPCException extends RuntimeException{
    public RPCException(RPCError error, String detail) {
        super(error.getMessage() + ": " + detail);
    }

    public RPCException(String message, Throwable cause) {
        super(message, cause);
    }

    public RPCException(RPCError error) {
        super(error.getMessage());
    }
}
