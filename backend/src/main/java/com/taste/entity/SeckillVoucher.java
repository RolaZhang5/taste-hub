package com.taste.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * The seckill voucher table has a one-to-one relationship with the voucher table
 *
 * @author RolaZhang
 * @since 2025-05-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_seckill_voucher")
public class SeckillVoucher implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Associated voucher ID
     */
    @TableId(value = "voucher_id", type = IdType.INPUT)
    private Long voucherId;

    /**
     * Stock
     */
    private Integer stock;

    /**
     * Creation time
     */
    private LocalDateTime createTime;

    /**
     * Begin time
     */
    private LocalDateTime beginTime;

    /**
     * Expiration time
     */
    private LocalDateTime endTime;

    /**
     * Update time
     */
    private LocalDateTime updateTime;


}
