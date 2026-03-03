package com.pm.apigateway.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service-default")
public interface AuthClient {
    @GetMapping("/validate")
    void validateToken(@RequestHeader("Authorization") String token);
}