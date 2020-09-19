package com.ysy.tmall.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.fastjson.JSON;
import com.ysy.tmall.common.exception.BizCodeEnum;
import com.ysy.tmall.common.utils.R;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * @anthor silenceYin
 * @date 2020/9/20 - 1:30
 *
 */
@Configuration
public class SentinelGatewayConfig {
    public SentinelGatewayConfig() {
        GatewayCallbackManager.setBlockHandler(((exchange, t) -> {
            R error = R.error(BizCodeEnum.TOO_MANY_REQUEST_EXCEPTION.getCode(), BizCodeEnum.TOO_MANY_REQUEST_EXCEPTION.getMessage());
            String jsonString = JSON.toJSONString(error);
            // Mono<ServerResponse> body = ServerResponse.ok().body(Mono.just(jsonString), String.class);
            return ServerResponse.ok().body(Mono.just(jsonString), String.class);
        }));
    }
}
