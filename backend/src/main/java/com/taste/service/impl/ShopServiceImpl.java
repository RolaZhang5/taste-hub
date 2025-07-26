package com.taste.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taste.dto.Result;
import com.taste.entity.Shop;
import com.taste.mapper.ShopMapper;
import com.taste.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taste.utils.CacheClient;
import com.taste.utils.RedisData;
import com.taste.utils.SystemConstants;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.taste.utils.RedisConstants.*;

/**
 * <p>
 * Service implementation class 
 * </p>
 *
 * @author
 * @since 2025-04-18
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private CacheClient cacheClient;

    @Override
    public Result queryById(Long id) {
//        Cache penetration
//        Shop shop = queryWithPassThrough(id);
        Shop shop = cacheClient.queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);
//        Use a mutex lock to avoid cache breakdown when a hot ket expires
//        Shop shop = queryWithMutex(id);

//       Apply logic control to ensure only one thread rebuilds the cache
//        Shop shop = queryWithLogicalExpire(id);
//        Shop shop = cacheClient.queryWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);
        if (shop == null) {
            return Result.fail("The shop does not exist!");
        }
        return Result.ok(shop);
    }

    public Result queryWithPassThrough(Long id) {
        String key = CACHE_SHOP_KEY + id;
//        1. Query Redis to check whether the shop exists
        String shopJson = stringRedisTemplate.opsForValue().get(key);

//        2. check if it exists
        if (!StrUtil.isBlank(shopJson)) {
            //        3. Return immediately if the data exists
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
//            stringRedisTemplate.expire(key, CACHE_SHOP_TTL, TimeUnit.MINUTES);
            return Result.ok(shop);
        }
//        Return if null
        if (shopJson != null) {
            return null;
        }
//        4. Query database by ID if the data does not exists in the cache
        Shop shop = getById(id);
//        5. Return 404, if the database does not exist
        if (shop == null) {
            //            Write an empty string
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
//        6. Write into Redis, if exist
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
//        stringRedisTemplate.expire(key, CACHE_SHOP_TTL, TimeUnit.MINUTES);
//        7. Return result
        return Result.ok(shop);
    }

    public Shop queryWithMutex(Long id) {
        String key = CACHE_SHOP_KEY + id;
//        1. Query Redis to check Whether the shop exists
        String shopJson = stringRedisTemplate.opsForValue().get(key);

//        2. Check whether the shop exists
        if (!StrUtil.isBlank(shopJson)) {
            //        3. Return if the shop exists
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
//            stringRedisTemplate.expire(key, CACHE_SHOP_TTL, TimeUnit.MINUTES);
            return shop;
        }
//        Filter out empty string values
        if (shopJson != null) {
            return null;
        }
        Shop shop = null;
        //        4. Implement cache rebuild logic
//        4.1 Acquire the mutex lock
        String lockKey = "lock:shop:" + id;
        try {
            boolean isLock = tryLock(lockKey);
//        4.2 Check if the mutex lock was acquired successfully
            //        4.3 if fails, sleep briefly and try again
            if (!isLock) {
                Thread.sleep(50);
                return queryWithMutex(id);
            }

//        4.4 if successful, query the database by ID 成功 ，根据id在数据库中查询
            shop = getById(id);
//            Simulate delay in cache rebuilding process
//            Thread.sleep(200);
//        5. Return 404 if data doesn't exits 数据库不存在，返回404
            if (shop == null) {
                //            Write an empty string to redis
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
//        6. Write to redis if exists
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
//        stringRedisTemplate.expire(key, CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
//            Release the lock
            unLock(lockKey);
        }
//        7. Return

        return shop;
    }

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public Shop queryWithLogicalExpire(Long id) {
        String key = CACHE_SHOP_KEY + id;
//        1. Query redis to check whether the shop exists
        String shopJson = stringRedisTemplate.opsForValue().get(key);

//        2. Check if it exists
        if (StrUtil.isBlank(shopJson)) {
//            3. If not hit,return null
            return null;
        }
//        4. If hit, deserialize JSON
        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
        Shop shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
//        5. Check the expiration time
        if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
            //        5.1 If not expire, return shop infomation
            return shop;
        }
//        5.2 If expired, needs to be rebuilt
        String lockShop = LOCK_SHOP_KEY + id;
        boolean lockFlag = tryLock(lockShop);
        if (lockFlag) {
//            6.3 If successful, start an independent thread pool to implement cache rebuild
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    System.out.println("Start rebuild");
                    saveShop2Redis(id, LOCK_SHOP_TTL);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    unLock(lockShop);
                }
            });
        }

//        7. Return
        return shop;
    }

    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unLock(String key) {
        stringRedisTemplate.delete(key);
    }

    public void saveShop2Redis(Long id, Long expireSeconds) throws InterruptedException {
        Shop shop = getById(id);
//        Thread.sleep(200);
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisData));
    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("The shop ID can't be null！");
        }
//        1. Update the database
        updateById(shop);
//        2. Delete the key from redis
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);
        return Result.ok();
    }

    @Override
    public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
//        1. Check whether a geo-based search is needed
        if (x == null || y == null) {
            Page<Shop> page = query().eq("type_id", typeId).page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
            return Result.ok(page);
        }
//        2.Calculate the pagination parameters
        int from = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
        int end = current * SystemConstants.DEFAULT_PAGE_SIZE;
//        3.Query results from Redis, ordered by geo distance and page, resule includes shopId, distance
        String key = SHOP_GEO_KEY + typeId.toString();
//        GEOSEARCH key BYLONLAT x y BYRADIUS 10 WITHDISTANCE
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo().search(
                key,
                GeoReference.fromCoordinate(x, y),
                new Distance(5000),
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance().limit(end));
        if (results == null) {
            return Result.ok(Collections.emptyList());
        }
//        4.1 sub list from to end part
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
        List<Long> ids = new ArrayList<>(list.size());
        Map<String, Distance> distanceMap = new HashMap<>(list.size());
        list.stream().skip(from).forEach(result ->{
//            4.2 get shopId
            String shopIdStr = result.getContent().getName();
            ids.add(Long.valueOf(shopIdStr));
//            4.3 get distance
            Distance distance = result.getDistance();
            distanceMap.put(shopIdStr, distance);
        });
        //        5.Query shop data from database by shopId and return the final result
        if (ids.size() <= from) {
//            no page
            return Result.ok();
        }
        List<Shop> shops = query().in("id", ids).last("order by field(id," + StrUtil.join(",", ids) + ")").list();
        for (Shop shop : shops) {
            shop.setDistance(distanceMap.get(shop.getId().toString()).getValue());
        }
        return Result.ok(shops);
    }

}
