package com.ysy.tmall.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @anthor silenceYin
 * @date 2020/7/16 - 21:38
 */
@Configuration
public class MyRedissonConfig {

    @Bean(destroyMethod="shutdown")
    public RedissonClient redisson() throws IOException {
        Config config = new Config();
       // config.useClusterServers()
        //        .addNodeAddress("127.0.0.1:7004", "127.0.0.1:7001");
        config.useSingleServer()
                .setPingConnectionInterval(1000)
                .setAddress("redis://129.211.93.117:6379");
;
        return Redisson.create(config);
    }
}
