package com.taste;

import com.taste.entity.Shop;
import com.taste.service.impl.ShopServiceImpl;

import com.taste.utils.CacheClient;
import com.taste.utils.RedisWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.taste.utils.RedisConstants.CACHE_SHOP_KEY;

@SpringBootTest
class TasteHubApplicationTests {
    @Resource
    private ShopServiceImpl shopService;
    @Resource
    private CacheClient cacheClient;
    @Resource
    private RedisWorker redisWorker;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testSaveShop() throws InterruptedException {
        Shop shop = shopService.getById(1L);
        cacheClient.setWithLogicalExpire(CACHE_SHOP_KEY + 1L, shop, 10L, TimeUnit.SECONDS);
//        shopService.saveShop2Redis(1L, 10L);
    }

    private ExecutorService es = Executors.newFixedThreadPool(500);

    @Test
    void testIdWorker() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);
        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                long id = redisWorker.nextId("order");
                System.out.println("id=" + id);
            }
            latch.countDown();
        };
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            es.submit(task);
        }
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("total time:" + (begin - end));
    }

    @Test
    void loadShopData() {
//        1.Query all shops info
        List<Shop> shops = shopService.query().list();
//        2.Group shops by typeId, put the same typeId in a set
        Map<Long, List<Shop>> map = shops.stream().collect(Collectors.groupingBy(Shop::getTypeId));
//        3.Batch write to Redis
        for (Map.Entry<Long, List<Shop>> entry : map.entrySet()) {
            Long typeId = entry.getKey();
            String key = "shop:geo:" + typeId;
            List<Shop> value = entry.getValue();
            List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>(value.size());
//            write to Redis GEOADD key x y member
            for (Shop shop : value) {
//                stringRedisTemplate.opsForGeo().add(key, new Point(shop.getX(), shop.getY()), shop.getId().toString());
                locations.add(new RedisGeoCommands.GeoLocation<>(shop.getId().toString(), new Point(shop.getX(), shop.getY())));
            }
            stringRedisTemplate.opsForGeo().add(key, locations);
        }
    }

    @Test
    void  testHyperLoglog(){
//        Prepare the array, composed of user data
        String[] users = new String[1000];
//        Index of the array
        int index = 0;
        for (int i = 0; i < 1000000; i++) {
            index = i % 1000;
            //        Value to be passed
            users[index] = "user_"+i;
            //        send after every 1000 entries
            if (index == 999) {
                stringRedisTemplate.opsForHyperLogLog().add("hll1", users);
            }
        }
//        Count the number of records
        Long size = stringRedisTemplate.opsForHyperLogLog().size("hll1");
        System.out.println("size =" + size);
    }
    @Test
    public void test() {
        List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                Consumer.from("g1", "c1"),
                StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                StreamOffset.create("stream.orders", ReadOffset.lastConsumed()));
        stringRedisTemplate.opsForValue().set("hello", "world");
        String val = stringRedisTemplate.opsForValue().get("hello");
        System.out.println("Redis says: " + val);
    }
}
