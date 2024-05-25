package com.example.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 用来给redis限流
 */
@Component
public class FlowUtils {
    @Resource
    StringRedisTemplate stringRedisTemplate;

    // 是否检查通过, blockTime冷却时间
    public boolean limitOnceCheck(String key, int blockTime) {
        if(Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
            return false;
        } else {
            stringRedisTemplate.opsForValue().set(key, "", blockTime, TimeUnit.SECONDS);
        }

        return true;
    }
}
