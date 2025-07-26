package com.taste.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taste.entity.Voucher;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper Interface
 * </p>
 *
 * @author RolaZhang
 * @since 2025-04-13
 */
public interface VoucherMapper extends BaseMapper<Voucher> {

    List<Voucher> queryVoucherOfShop(@Param("shopId") Long shopId);
}
