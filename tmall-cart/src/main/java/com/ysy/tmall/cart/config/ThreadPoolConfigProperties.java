package com.ysy.tmall.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 *
 * 线程池配置类
 * @anthor silenceYin
 * @date 2020/7/22 - 2:33
 */
@ConfigurationProperties(prefix = "ysymall.thread")
@Component
@Data
public class ThreadPoolConfigProperties {
    //核心线程大小
    private Integer coreSize;
    //最大线程数
    private Integer maxSize;
    //休眠时长
    private Integer keepAliveTime;
}
