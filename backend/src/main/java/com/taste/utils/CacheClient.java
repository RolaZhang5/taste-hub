package com.taste.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.taste.utils.RedisConstants.*;

@Slf4j
@Component
public class CacheClient {
    private final StringRedisTemplate stringRedisTemplate;

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
//        Set logic expiration time
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    public <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallBack, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
//        1. Check if the shop exists in Redis
        String json = stringRedisTemplate.opsForValue().get(key);

//        2. Check if it exists
        if (!StrUtil.isBlank(json)) {
            //        3. Return the result if it exists
            return JSONUtil.toBean(json, type);
        }
//        5. Check if the result is null
        if (json != null) {
            return null;
        }
//        Query data from the database by ID if it deos not exist
        R r = dbFallBack.apply(id);
        if (r == null) {
            stringRedisTemplate.opsForValue().set(key, "", time, unit);
            return null;
        }
//        6. Write to Redis if it exists 存在，写入redis
        this.set(key, r, time, unit);
//        7. Return result
        return r;
    }

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public <R, ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallBack, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
//        1. Check if the shop exists in Redis
        String json = stringRedisTemplate.opsForValue().get(key);

//        2. Check if it exists
        if (StrUtil.isBlank(json)) {
//            3. Return null if it is not found
            return null;
        }
//        4. if it hits, deserialize the JSON
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R r = JSONUtil.toBean((JSONObject) redisData.getData(), type);
//        5. Check the expiration time
        if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
            //        5.1 Return shop infomation if it has not expired
            return r;
        }
//        5.2 Rebuild the cache if expired
        String lockShop = LOCK_SHOP_KEY + id;
        boolean lockFlag = tryLock(lockShop);
        if (lockFlag) {
//            6.3 Start an independent thread pool to perform Cache rebuilding if lock is acquired successfully
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    System.out.println("Start rebuilding");
                    R r1 = dbFallBack.apply(id);
                    this.setWithLogicalExpire(key,r1,time, unit);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    unLock(lockShop);
                }
            });
        }
//        7. Return result
        return r;
    }

    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unLock(String key) {
        stringRedisTemplate.delete(key);
    }
}
