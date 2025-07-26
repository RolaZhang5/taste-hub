package com.taste.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
 * @since 2025-04-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_voucher")
public class Voucher implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Shop ID
     */
    private Long shopId;

    /**
     * Voucher title
     */
    private String title;

    /**
     * SubTitle
     */
    private String subTitle;

    /**
     * Usage rules
     */
    private String rules;

    /**
     * Payment amount
     */
    private Long payValue;

    /**
     * Deduction amount
     */
    private Long actualValue;

    /**
     * Voucher type
     */
    private Integer type;

    /**
     * Voucher status
     */
    private Integer status;
    /**
     * Stock
     */
    @TableField(exist = false)
    private Integer stock;

    /**
     * Effective time
     */
    @TableField(exist = false)
    private LocalDateTime beginTime;

    /**
     * Expiration time
     */
    @TableField(exist = false)
    private LocalDateTime endTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
