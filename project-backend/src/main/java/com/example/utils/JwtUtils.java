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

import java.time.Instant;
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

    public boolean doInvalid(String headToken) { //讓jwt token失效
        //System.out.println("token is ? " + headToken);
        String token = truncToken(headToken);
        //System.out.println("处理之后token为" + token);
        if(token == null) {
            return false;
        }
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier verifier = JWT.require(algorithm).build(); //decode
        //System.out.println("jwrVerifier is " + verifier);
        try {
            DecodedJWT jwt = verifier.verify(token);
            String id = jwt.getId();
//            System.out.println("decode之后的id is " + id);
            return deleteToken(id, jwt.getExpiresAt());
        } catch (JWTVerificationException e) {
//            System.out.println("出现了JWTVerificationException");
            e.printStackTrace(); // 打印堆栈跟踪以获取更多信息
            return false;
        }
    }

    public boolean isInvalid(String id) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(Config.JWT_BLACKLIST + id));  // Redis裏還有沒有UUID&&防止null
    }

    public boolean deleteToken(String id, Date date) {
        if( this.isInvalid(id) ) {return false;}

        Date now = new Date();
        long expire =  Math.max(0, date.getTime() - now.getTime());
        stringRedisTemplate.opsForValue().set(Config.JWT_BLACKLIST + id, "", expire, TimeUnit.MILLISECONDS);
        return true;

    }

    public Date expireTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, expire*24);
        return calendar.getTime();
    }

    public UserDetails toUserDetails(DecodedJWT decodedJWT) {
        Map<String, Claim> claims = decodedJWT.getClaims();
//        System.out.println(claims);
        return User
                .withUsername(claims.get("name").toString())
//                .withUsername(claims.get("username").toString())
//                .password(claims.get("password").toString())
                .password("******")
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

        if(token == null || !token.startsWith("Bearer ")) {
            System.out.println("token is null or err");
            if(token!=null && !token.startsWith("Bearer ")) System.out.println("不是Bearer开头！");
            return null;
        }
        return token.substring(7);
    }

    public String generateToken(UserDetails details, int id, String username) {
        Algorithm algorithm = Algorithm.HMAC256(key);
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString()) //利用id比較好查詢
                .withClaim("id", id)
                .withClaim("name", username)
                .withClaim("authorities", details.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority).toList())
                .withExpiresAt(expireTime())
                .withIssuedAt(new Date())  //start time
                .sign(algorithm); // 加密
    }

}
