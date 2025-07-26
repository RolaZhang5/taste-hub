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
 *
 * @author RolaZhang
 * @since 2025-04-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_voucher_order")
public class VoucherOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /**
     * Ordering user ID
     */
    private Long userId;

    /**
     * Voucher ID
     */
    private Long voucherId;

    /**
     * Payment methods 1: Account balance 2: Alipay 3:Wechat pay
     */
    private Integer payType;

    /**
     * Order status: 1: Unpaid; 2: Paid; 3: Used; 4: Canceled; 5: Refunding; 6:Refounded
     */
    private Integer status;

    /**
     * Order time
     */
    private LocalDateTime createTime;

    /**
     * Paid time
     */
    private LocalDateTime payTime;

    /**
     * used time
     */
    private LocalDateTime useTime;

    /**
     * Refund time
     */
    private LocalDateTime refundTime;

    /**
     * Update time
     */
    private LocalDateTime updateTime;


}
