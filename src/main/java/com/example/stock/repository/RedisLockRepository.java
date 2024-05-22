package com.example.stock.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisLockRepository {

    //redis 명령 수행을 위한 redis 탬플릿
    private RedisTemplate<String, String> redisTemplate;

    public RedisLockRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    //setNX 명령어 활용을 위한 lock 메서드
    public Boolean lock(Long key) {
        return redisTemplate
                .opsForValue().
                setIfAbsent(generateKey(key), "lock", Duration.ofMillis(3_000));
        //setnx {stockID} {"lock"}
    }

    //로직 수행 완료후 lock 해제
    public Boolean unlock(Long key) {
        return redisTemplate.delete(generateKey(key));
    }

    private String generateKey(Long key) {
        return key.toString();
    }
}
