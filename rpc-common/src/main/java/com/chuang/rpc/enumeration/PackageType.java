package com.chuang.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 包类型，用于序列化请求和响应时，区分当前数据是哪种数据包
 * 包含request和response两种
 * */
@Getter
@AllArgsConstructor
public enum PackageType {

    REQUEST_PACK(0),
    RESPONSE_PACK(1);

    private final int code;

}
