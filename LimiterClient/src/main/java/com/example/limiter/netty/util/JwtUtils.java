package com.example.limiter.netty.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

/**
 * @author feng xud
 */
public class JwtUtils {
    /**
     * 密钥
     */
    static final String SECRET = "jwt_secret_key";

    /**
     * 过期时间 // 30分钟
     */
    static final long EXPIRATION_TIME = 30 * 60 * 1000;

    // 生成JWT令牌
    public static String generateToken(String userId, String userName) {

        Date expirationTime = new Date(System.currentTimeMillis() + EXPIRATION_TIME);

        String token = Jwts.builder()
                .claim("userId", userId)
                .claim("userName", userName)
                .setExpiration(expirationTime)
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();

        return token;
    }

    // 解析JWT令牌
    public static Claims parseToken(String token) {
        Claims claims = null;
        try {
            claims = Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
                    .getBody();
        }catch (Exception e){
            e.printStackTrace();
        }
        return claims;
    }

    public static void main(String[] args) {
        String fxd = JwtUtils.generateToken("1", "fxd");
        System.out.println(fxd);
    }
}
