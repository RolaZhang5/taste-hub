package com.taste.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taste.dto.Result;
import com.taste.entity.VoucherOrder;
import com.taste.mapper.VoucherOrderMapper;
import com.taste.service.ISeckillVoucherService;
import com.taste.service.IVoucherOrderService;
import com.taste.utils.RedisWorker;
import com.taste.utils.UserHolder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>
 * Service implementation class 
 * </p>
 *
 * @author
 * @since 2025-04-16
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private RedisWorker redisWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    //    private BlockingQueue<VoucherOrder> orderTasks = new ArrayBlockingQueue<>(1024 * 1024);
    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    @PostConstruct
    private void init() {
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }
    private class VoucherOrderHandler implements Runnable {
        String queueName = "stream.orders";
        private volatile boolean running = true;
        @PreDestroy
        public void stop() {
            running = false;
        }
        @Override
        public void run() {
            while (running) {
//                1.get block queue info
                try {
//                    1.get order info from redis stream queue XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS stream.order >
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(queueName, ReadOffset.lastConsumed()));
//                    2.check whether message retrieval from the stream was success
                    if (list == null || list.isEmpty()) {
                        // 2.1 if no message retrieval, it means the stream is empty,continue to the next loop iteration
                        continue;
                    }
//                    3 if the message is successfully retrieved, proceed to place the order
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);
                    handleVoucherOrder(voucherOrder);
//                    4 confirm ACK
                    stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
                } catch (Exception e) {
                    log.error("Error handling orders!", e);
                    handlePendingList();
                }
            }
        }

        private void handlePendingList() {
            while (true) {
//                1.get block queue info
                try {
//                    1.get order info from redis stream pending-list XREADGROUP GROUP g1 c1 COUNT 1 STREAMS streams.order >
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1),
                            StreamOffset.create(queueName, ReadOffset.from("0")));
//                    2.check whether message retrieval from the stream was success
                    if (list == null || list.isEmpty()) {
                        // 2.1 if no message retrieval, it means pending-list is empty,break the loop iteration
                        break;
                    }
//                    3 if the message is successfully retrieved, proceed to place the order
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);
                    handleVoucherOrder(voucherOrder);
//                    4 confirm ACK
                    stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
                } catch (Exception e) {
                    log.error("Error handling orders!", e);
//                    stop from too many times to execute
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        /**
         * private class VoucherOrderHandler implements Runnable {
         *
         * @Override public void run() {
         * while (true) {
         * //                1.get block queue info
         * try {
         * VoucherOrder voucherOrder = orderTasks.take();
         * handleVoucherOrder(voucherOrder);
         * } catch (InterruptedException e) {
         * log.error("Error handling orders!", e);
         * }
         * }
         * }
         */
        private void handleVoucherOrder(VoucherOrder voucherOrder) {
            RLock lock = redissonClient.getLock("order:" + voucherOrder.getUserId());
            // default 30s lock get release
            boolean isLock = lock.tryLock();
//        Check if the lock was acquired successfully
            if (!isLock) {
                log.error("Duplicate orders are not allow！");
                return;
            }
            try {
                //            Get the proxy objext(for transaction)
                proxy.createVoucherOrder(voucherOrder);
                return;
            } finally {
                lock.unlock();
            }
        }
    }

    private IVoucherOrderService proxy;

    @Override
    public Result seckillVoucher(Long voucherId) {
//            get user
        Long userId = UserHolder.getUser().getId();
//        get orderId
        long orderId = redisWorker.nextId("order");
//        1.execute lua srcipt
        Long result = stringRedisTemplate.execute(SECKILL_SCRIPT, Collections.emptyList(), voucherId.toString(), userId.toString(), String.valueOf(orderId));
        //        2.check if the result is 0 or not
        int r = result.intValue();
//        2.1 if not 0 means no right to purchase
        if (r != 0)
            return Result.fail(r == 1 ? "Out of stock" : "Duplicate orders are not allowed");

//        3. get the proxy obj
        proxy = (IVoucherOrderService) AopContext.currentProxy();
//        4. return the order id
        return Result.ok(orderId);
    }

    /**
     * @param
     * @return
     * @Override public Result seckillVoucher(Long voucherId) {
     * //            get user
     * Long userId = UserHolder.getUser().getId();
     * //        1.execute lua srcipt
     * Long result = stringRedisTemplate.execute(SECKILL_SCRIPT, Collections.emptyList(), voucherId.toString(), userId.toString());
     * //        2.check if the result is 0 or not
     * int r = result.intValue();
     * //        2.1 if not 0 means no right to purchase
     * if (r != 0)
     * return Result.fail(r == 1 ? "Out of stock" : "Duplicate orders are not allowed");
     * //        2.2 if equal 0 means have right to purchase, save order info to block queue
     * long orderId = redisWorker.nextId("order");
     * VoucherOrder voucherOrder = new VoucherOrder();
     * voucherOrder.setId(orderId);
     * voucherOrder.setUserId(userId);
     * voucherOrder.setVoucherId(voucherId);
     * orderTasks.add(voucherOrder);
     * //        3. get the proxy obj
     * proxy = (IVoucherOrderService) AopContext.currentProxy();
     * //        4. return the order id
     * return Result.ok(orderId);
     * }
     */
    @Transactional
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        int count = query().eq("user_id", voucherOrder.getUserId()).eq("voucher_id", voucherOrder.getVoucherId()).count();
        if (count > 0) {
            log.error("The same user can only place one order！");
            return;
        }
//        4. if in stock, decrease the stock quantity
        boolean success = seckillVoucherService.update().setSql("stock = stock -1").eq("voucher_id", voucherOrder.getVoucherId()).gt("stock", 0).update();
        if (!success) {
            log.error("Out of stock！");
            return;
        }
        save(voucherOrder);
    }

//    @Override
//    public Result seckillVoucher(Long voucherId) {
////        1. Check seckill voucher
//        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
////        2. Check seckill has started or finished
//        if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
//            return Result.fail("Seckill has not started yet!");
//        }
//        if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
//            return Result.fail("Seckill has finished！");
//        }
////        3. Check if the voucher is in stock
//        if (voucher.getStock() < 1) {
////            3.1 Return error if out of stock
//            return Result.fail("Out of stock！");
//        }
//        Long userId = UserHolder.getUser().getId();
////        synchronized (userId.toString().intern()) {
////        SimpleRedisLock lock = new SimpleRedisLock("order:" + userId, stringRedisTemplate);
////        Fetch the lock
////        boolean isLock = lock.tryLock(1200);
//        RLock lock = redissonClient.getLock("order:" + userId);
//        // default 30s lock get release
//        boolean isLock = lock.tryLock();
////        Check if the lock was acquired successfully
//        if (!isLock) {
//            return Result.fail("Duplicate orders are not allowed!");
//        }
//        try {
//            //            Fetch proxy object(for transation)
//            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
//            return proxy.createVoucherOrder(voucherId);
//        } finally {
////            lock.unLock();
//            lock.unlock();
//        }
//
//    }

    /**
     *
     * @param voucherId
     * @return
     @Transactional public Result createVoucherOrder(Long voucherId) {
     //       Gett user infomation
     Long userId = UserHolder.getUser().getId();
     int count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
     if (count > 0) {
     return Result.fail("Duplicate orders are not allowed!");
     }
     //        4. if is in stock, decrease the stock quantity
     boolean success = seckillVoucherService.update().setSql("stock = stock -1").eq("voucher_id", voucherId).gt("stock", 0).update();
     if (!success) {
     return Result.fail("Out of stock!");
     }
     //        5. Create a voucher order and return the order ID
     VoucherOrder voucherOrder = new VoucherOrder();
     long orderId = redisWorker.nextId("order");
     voucherOrder.setId(orderId);
     voucherOrder.setUserId(userId);
     voucherOrder.setVoucherId(voucherId);
     save(voucherOrder);
     return Result.ok(orderId);
     }
     */
}
