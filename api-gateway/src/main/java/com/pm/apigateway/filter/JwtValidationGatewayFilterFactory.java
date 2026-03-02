package com.pm.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;


@Component
public class JwtValidationGatewayFilterFactory extends
        AbstractGatewayFilterFactory<Object> {

        private static final Logger log = LoggerFactory.getLogger(JwtValidationGatewayFilterFactory.class);

        private final WebClient webClient;

        public JwtValidationGatewayFilterFactory(WebClient.Builder webClientBuilder,
                                                 @Value("${auth.service.url}") String authServiceUrl) {
            log.info("=====authServiceUrl: {}", authServiceUrl);
            this.webClient = webClientBuilder.baseUrl(authServiceUrl).build();
        }
    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            String token =
                    exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            log.info("=====Token: {}", token);

            if (token == null || !token.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            return webClient.get()
                    .uri("/validate")
                    .header(HttpHeaders.AUTHORIZATION, token) // Use sanitized token
                    .retrieve()
                    .toBodilessEntity()
                    .flatMap(response -> chain.filter(exchange))
                    .onErrorResume(e -> {
                        log.error("=====onErrorResume: {}", e.toString());
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });

//            return webClient.get()
//                    .uri("http://auth-service:4005/validate")
//                    .header("Authorization", token)
//                    .retrieve()
//                    .toBodilessEntity()
//                    .then(chain.filter(exchange));
        };
    }
}