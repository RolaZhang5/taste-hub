package com.taste.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taste.dto.Result;
import com.taste.entity.Voucher;
import com.taste.mapper.VoucherMapper;
import com.taste.entity.SeckillVoucher;
import com.taste.service.ISeckillVoucherService;
import com.taste.service.IVoucherService;
import com.taste.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  Service implementation class 
 * </p>
 *
 * @author RolaZhang
 * @since 2025-04-13
 */
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryVoucherOfShop(Long shopId) {
        // Query voucher infomation
        List<Voucher> vouchers = getBaseMapper().queryVoucherOfShop(shopId);
        // Return result
        return Result.ok(vouchers);
    }

    @Override
    @Transactional
    public void addSeckillVoucher(Voucher voucher) {
        // Save the voucher
        save(voucher);
        // Save the seckill infomation
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);

        //save stock in redis
        stringRedisTemplate.opsForValue().set(RedisConstants.SECKILL_STOCK_KEY+ voucher.getId(), voucher.getStock().toString());
    }
}
