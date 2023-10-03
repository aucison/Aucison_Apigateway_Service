package com.example.newapigatewayservice.config.security;

import com.example.Aucison_Member_Service.dto.MemberDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class JwtUtils {

    @Value("${token.access-token-time}")
    private long accessTokenTime;
    @Value("${token.refresh-token-time}")
    private long refreshTokenTime;
    @Value("${token.secret}")
    private String secretKey;
    private final StringRedisTemplate stringRedisTemplate;

    public String createAccessToken(MemberDto memberDto) {
        Claims claims = Jwts.claims();
        claims.put("email", memberDto.getEmail());
//        claims.put("nickname", memberDto.getNickname());
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String createRefreshToken(MemberDto memberDto) { // 새로 만들기 전에 기존 refreshToken 지우고 만들기
        Claims claims = Jwts.claims();
        claims.put("email", memberDto.getEmail());
//        claims.put("nickname", memberDto.getNickname());
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public void updateRefreshToken(MemberDto memberDto, String refreshToken) { // 만든 refreshToken redis에 저장하는 함수
        stringRedisTemplate.opsForValue().set(memberDto.getEmail(), refreshToken, refreshTokenTime, TimeUnit.MILLISECONDS);
    }

    public String getRefreshToken(String email) { // 사용자 구글 이메일(로그인 이메일)로 refreshToken 조회하는 함수
        return stringRedisTemplate.opsForValue().get(email);
    }

    public void deleteRefreshToken(String email) { // 사용자 구글 이메일(로그인 이메일)로 refreshToken 삭제하는 함수
        if (getRefreshToken(email) != null) {
            stringRedisTemplate.delete(email);
        }
    }

//    토큰 유효성 검증
//    => apigateway-service로 옮기기

//    public boolean validateToken(String token) {
//        if(!StringUtils.hasText(token)) {
//            throw new RuntimeException();
//        }
//        if (isLogout(token)) {
//            throw new RuntimeException();
//        }
//        try {
//            Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
//            return true;
//        } catch (SignatureException | MalformedJwtException e) {
//            throw new RuntimeException(e.getMessage());
//        } catch (ExpiredJwtException e) {
//            throw new RuntimeException(e.getMessage());
//        }
//    }

    public void setBlackList(String accessToken) { // 로그아웃 시 redis에 만료된 accessToken이라고 저장하는 함수
        Long expiration = getExpiration(accessToken);
        stringRedisTemplate.opsForValue().set(accessToken, "logout", expiration, TimeUnit.MILLISECONDS);
    }

    public boolean isLogout(String accessToken) {
        return !ObjectUtils.isEmpty(stringRedisTemplate.opsForValue().get(accessToken));
    }

    public Long getExpiration(String token) {
        Date expiration = getClaims(token).getExpiration();
        return expiration.getTime() - new Date().getTime();
    }

    public String getEmailFromToken(String token) {
        return (String) getClaims(token).get("email");
    }

//    public String getNickNameFromToken(String token) {
//        return (String) getClaims(token).get("nickname");
//    }

    public Claims getClaims(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

    public String resolveAccessToken(HttpServletRequest request) {
        String jwtHeader = request.getHeader("Authorization");
        if (jwtHeader != null && jwtHeader.startsWith("Bearer ")) {
            return jwtHeader.replace("Bearer ", "");
        } else {
            return null;
        }
    }
}
