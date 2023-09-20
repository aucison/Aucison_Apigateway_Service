package com.example.newapigatewayservice.filter;

import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    Environment env;

    public AuthorizationHeaderFilter(Environment env) {
        super(Config.class);
        this.env = env;
    }

    public static class Config {

    }

    @Override
    public GatewayFilter apply(AuthorizationHeaderFilter.Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "No authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String jwt = authorizationHeader.replace("Bearer", "");

            if(!isJwtValid(jwt)) {
                return onError(exchange, "JWT token is not valid", HttpStatus.UNAUTHORIZED);
            }

            if (isJwtExpired(jwt)) {
                return onError(exchange, "JWT token has expired", HttpStatus.UNAUTHORIZED);
            }

            return chain.filter(exchange);
        });
    }

    private boolean isJwtValid(String jwt) {
        boolean returnValue = true;
        String subject = null;

        try {
            Key secretKey = Keys.hmacShaKeyFor(env.getProperty("token.secret").getBytes(StandardCharsets.UTF_8));
            JwtParserBuilder jwtParserBuilder = Jwts.parserBuilder();
            JwtParserBuilder jwtParserBuilder1 = jwtParserBuilder.setSigningKey(secretKey);
            subject = jwtParserBuilder1
                    .build()
                    .parseClaimsJws(jwt).getBody()
                    .getSubject();
        } catch (Exception ex) {
            returnValue = false;
        }

        if(subject == null || subject.isEmpty()) {
            returnValue = false;
        }

        return returnValue;
    }

    private boolean isJwtExpired(String jwt) {
        boolean isExpired = false;

        try {
            Key secretKey = Keys.hmacShaKeyFor(env.getProperty("token.secret").getBytes(StandardCharsets.UTF_8));
            JwtParserBuilder jwtParserBuilder = Jwts.parserBuilder();
            JwtParserBuilder jwtParserBuilder1 = jwtParserBuilder.setSigningKey(secretKey);
            Date expiration = jwtParserBuilder1
                    .build()
                    .parseClaimsJws(jwt).getBody()
                    .getExpiration();

            // 현재 시간과 토큰의 만료 시간 비교
            Date currentTime = new Date();
            if (expiration != null && expiration.before(currentTime)) {
                isExpired = true;
            }
        } catch (Exception ex) {
            isExpired = true;
        }

        return isExpired;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);

        log.error(err);
        return response.setComplete();
    }
}