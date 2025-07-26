package com.taste.controller;


import com.taste.dto.Result;
import com.taste.entity.Voucher;
import com.taste.service.IVoucherService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 *
 * @author RolaZhang
 * @since 2025-04-13
 */
@RestController
@RequestMapping("/voucher")
public class VoucherController {

    @Resource
    private IVoucherService voucherService;

    /**
     * Add a regular voucher
     * @param voucher voucher infomation
     * @return voucher id
     */
    @PostMapping
    public Result addVoucher(@RequestBody Voucher voucher) {
        voucherService.save(voucher);
        return Result.ok(voucher.getId());
    }

    /**
     * Add a seckill voucher
     * @param voucher voucher details，including seckill infomation
     * @return voucher ID
     */
    @PostMapping("seckill")
    public Result addSeckillVoucher(@RequestBody Voucher voucher) {
        voucherService.addSeckillVoucher(voucher);
        return Result.ok(voucher.getId());
    }

    /**
     * Query the list of voucher type lists for the shop
     * @param shopId shop ID
     * @return voucher list
     */
    @GetMapping("/list/{shopId}")
    public Result queryVoucherOfShop(@PathVariable("shopId") Long shopId) {
       return voucherService.queryVoucherOfShop(shopId);
    }
}
