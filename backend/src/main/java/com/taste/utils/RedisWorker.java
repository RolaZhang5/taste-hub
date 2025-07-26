package com.taste.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisWorker {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    /**
    The start timestamp
     */
    private static final long BEGIN_TIMESTAMP = 1640995200L;
    // Number of bits for the sequence number
    private static final int COUNT_BIT = 32;
    public long nextId(String keyPrex){
//       Generate timestamp
        LocalDateTime now = LocalDateTime.now();
        long newSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timeStamp = newSecond - BEGIN_TIMESTAMP;

//       2. Generate the sequence number
//        2.1 Get the current date, precise to day
        String date= now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
//        2.2 Auto-increment
        Long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrex + ":" + date);
//    3. Combine and return
        return timeStamp<<COUNT_BIT|count;
    }
}
