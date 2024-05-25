package com.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtils {
    @Value("${spring.security.jwt.key}")
    String key;

    @Value("${spring.security.jwt.expire}")
    int expire;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    public boolean doInvalid(String token) { //讓jwt token失效
        token = truncToken(token);
        if(token == null) {return false;}
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier verifier = JWT.require(algorithm).build(); //decode
        try {
            DecodedJWT jwt = verifier.verify(token);
            String id = jwt.getClaim("id").asString();
            return deleteToken(id, jwt.getExpiresAt());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public boolean isInvalid(String id) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(Config.JWT_BLACKLIST + id));  // Redis裏還有沒有UUID&&防止null
    }

    public boolean deleteToken(String id, Date date) {
        if( this.isInvalid(id) ) {return false;}
        else {
            Date now = new Date();
            long expire =  Math.max(0, date.getTime() - now.getTime());
            stringRedisTemplate.opsForValue().set(Config.JWT_BLACKLIST + id, "", expire, TimeUnit.MILLISECONDS);
            return true;
        }
    }

    public Date expireTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, expire*24);
        return calendar.getTime();
    }

    public UserDetails toUserDetails(DecodedJWT decodedJWT) {
        Map<String, Claim> claims = decodedJWT.getClaims();
        return User
                .withUsername(claims.get("username").toString())
                .password(claims.get("password").toString())
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }

    public int toId(DecodedJWT decodedJWT) {
        Map<String, Claim> claims = decodedJWT.getClaims();
        return claims.get("id").asInt();
    }
    public DecodedJWT decode(String token) {
        Algorithm algorithm = Algorithm.HMAC256(key);
        token = truncToken(token);
        if(token == null) {return null;}
        JWTVerifier verifier = JWT.require(algorithm).build();
        try {
            DecodedJWT jwt = verifier.verify(token); //check if token is changed illegally
            if( isInvalid(jwt.getId()) ) { //if拉黑了
                return null;
            }
            Date date = jwt.getExpiresAt(); //查看是否過期
            if(new Date().after(date)) {
                return null; //過期了
            } else {
                return jwt;
            }
        } catch (JWTVerificationException e) {
            return null;
        }

    }

    private String truncToken(String token) {
        if(token == null || !token.startsWith("Bearer ")) return null;
        return token.substring(7);
    }

    public String generateToken(UserDetails details, int id, String username) {
        Algorithm algorithm = Algorithm.HMAC256(key);
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString()) //利用id比較好查詢
                .withClaim("id", id)
                .withClaim("username", username)
                .withClaim("authorities", details.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority).toList())
                .withExpiresAt(new Date(System.currentTimeMillis() + expire*24))
                .withIssuedAt(new Date())  //start time
                .sign(algorithm); // 加密
    }

}
