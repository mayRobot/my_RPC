package com.chuang.rpc.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data               //作用于类上，是以下注解的集合：@ToString @EqualsAndHashCode @Getter @Setter @RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor //全参构造器
public class HelloObject implements Serializable {
    // 需要从客户端传递给服务端，因此需要实现序列化接口
    private Integer id;
    private String message;
}
