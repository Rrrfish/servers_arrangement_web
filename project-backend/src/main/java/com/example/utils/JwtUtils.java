package com.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {
    @Value("${spring.security.jwt.key}")
    String key;

    @Value("${spring.security.jwt.expire}")
    int expire;

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
                .withClaim("id", id)
                .withClaim("username", username)
                .withClaim("authorities", details.getAuthorities().stream().toList())
                .withExpiresAt(new Date(System.currentTimeMillis() + expire*24))
                .withIssuedAt(new Date())  //start time
                .sign(algorithm); // 加密
    }

}
