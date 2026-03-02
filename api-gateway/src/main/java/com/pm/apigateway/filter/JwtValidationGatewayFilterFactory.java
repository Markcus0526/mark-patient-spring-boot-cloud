package com.pm.apigateway.filter;

import com.pm.apigateway.client.AuthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Component
public class JwtValidationGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtValidationGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(JwtValidationGatewayFilterFactory.class);

    private final AuthClient authClient;

    public JwtValidationGatewayFilterFactory(@Lazy AuthClient authClient) {
        super(Config.class);
        this.authClient = authClient;
    }

    public static class Config { }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            log.info("=====Token=====: {}", token);

            if (token == null || !token.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // ⚠️ CRITICAL: Wrap the blocking Feign call in a Bounded Elastic scheduler
            return Mono.fromRunnable(() -> authClient.validateToken(token))
                    .subscribeOn(Schedulers.boundedElastic())
                    .then(chain.filter(exchange))
                    .onErrorResume(e -> onError(exchange, HttpStatus.UNAUTHORIZED));
        };
    }

    private Mono<Void> onError(org.springframework.web.server.ServerWebExchange exchange, HttpStatus status) {
        log.error("=====onError=====: {}", status.toString());

        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

//    @Override
//    public GatewayFilter apply(Object config) {
//        return (exchange, chain) -> {
//            String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
//
//            log.info("=====Token=====: {}", token);
//
//            if (token == null || !token.startsWith("Bearer ")) {
//                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//                return exchange.getResponse().setComplete();
//            }
//
//            return webClient.get()
//                    .uri("/validate")
//                    .header(HttpHeaders.AUTHORIZATION, token) // Use sanitized token
//                    .retrieve()
//                    .toBodilessEntity()
//                    .flatMap(response -> chain.filter(exchange))
//                    .onErrorResume(e -> {
//                        log.error("=====onErrorResume=====: {}", e.toString());
//                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//                        return exchange.getResponse().setComplete();
//                    });
//
//        };
//    }
}