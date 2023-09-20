package com.example.newapigatewayservice.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthFilter implements GatewayFilterFactory {

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            return chain.filter(exchange);
        };
    }

    @Override
    public String name() {
        return "CustomLoginFilter";
    }
}
