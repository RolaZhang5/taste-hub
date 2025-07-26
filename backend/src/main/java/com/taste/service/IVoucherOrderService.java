package com.taste.service;

import com.taste.dto.Result;
import com.taste.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  Service class
 * </p>
 *
 * @author RolaZhang
 * @since 2025-04-16
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    Result seckillVoucher(Long voucherId);

    void createVoucherOrder(VoucherOrder voucherOrder);
}
