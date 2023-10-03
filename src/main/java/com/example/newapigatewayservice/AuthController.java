package com.example.newapigatewayservice;

import com.example.newapigatewayservice.config.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@RestController
@RequestMapping("/member-service")
public class AuthController {

    private final JwtUtils jwtUtils;
    private final RestTemplate restTemplate;

    @PostMapping("/reissue")
    public ResponseEntity reissue(@RequestHeader("refreshToken") String refreshToken) {
        return reissueToken(refreshToken);
    }

    public ResponseEntity reissueToken(String refreshToken) { // 토큰 재발행

        String email = jwtUtils.getEmailFromToken(refreshToken);
        jwtUtils.deleteRefreshToken(email);
        // 이메일 정보를 사용하여 login 서비스 호출
        String loginServiceUrl = "member-service-URL"; // 로그인 서비스의 URL을 설정하세요
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        // 이메일을 JSON 형식으로 전달
        String requestBody = "{\"email\": \"" + email + "\"}";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        // POST 요청을 통해 login 서비스 호출
        ResponseEntity<String> loginResponse = restTemplate.exchange(
                loginServiceUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // login 서비스의 응답을 그대로 반환
        return loginResponse;
    }
}
