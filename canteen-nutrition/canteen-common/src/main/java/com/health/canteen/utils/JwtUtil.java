package com.health.canteen.utils;

import com.health.canteen.context.LoginUser;
import com.health.canteen.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT令牌生成与解析工具
 */
@Slf4j
@Component
public class JwtUtil {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_MERCHANT_ID = "merchantId";

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 生成令牌
     */
    public String createToken(LoginUser loginUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, loginUser.getUserId());
        claims.put(CLAIM_USERNAME, loginUser.getUsername());
        claims.put(CLAIM_ROLE, loginUser.getRole());
        if (loginUser.getMerchantId() != null) {
            claims.put(CLAIM_MERCHANT_ID, loginUser.getMerchantId());
        }
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtProperties.getTtl()))
                .signWith(secretKey())
                .compact();
    }

    /**
     * 解析令牌，令牌缺失、被篡改或已过期时返回null
     */
    public LoginUser parseToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            LoginUser loginUser = new LoginUser();
            loginUser.setUserId(toInteger(claims.get(CLAIM_USER_ID)));
            loginUser.setUsername((String) claims.get(CLAIM_USERNAME));
            loginUser.setRole(toInteger(claims.get(CLAIM_ROLE)));
            loginUser.setMerchantId(toInteger(claims.get(CLAIM_MERCHANT_ID)));
            return loginUser;
        } catch (Exception e) {
            log.warn("令牌解析失败: {}", e.getMessage());
            return null;
        }
    }

    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    private Integer toInteger(Object value) {
        return value == null ? null : Integer.valueOf(value.toString());
    }

}
