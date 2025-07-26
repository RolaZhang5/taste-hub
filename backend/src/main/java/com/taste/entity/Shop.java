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
 * @since 2025-04-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_shop")
public class Shop implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Shop name
     */
    private String name;

    /**
     * Shop type ID
     */
    private Long typeId;

    /**
     * Shop images, mutiple image URLs separated by ";"
     */
    private String images;

    /**
     * Business district
     */
    private String area;

    /**
     * Address
     */
    private String address;

    /**
     * Latitude
     */
    private Double x;

    /**
     * Longitude
     */
    private Double y;

    /**
     * Average(rounded to integer)
     */
    private Long avgPrice;

    /**
     * Sales quantity
     */
    private Integer sold;

    /**
     * comments
     */
    private Integer comments;

    /**
     * Rating score ranges from 1-5 scores，
     * Stored as an integer multiplied by 10 to avoid floating-point precision issues.
     */
    private Integer score;

    /**
     * Business hours, e.g. 10:00-22:00
     */
    private String openHours;

    /**
     * Creation time
     */
    private LocalDateTime createTime;

    /**
     * Update time
     */
    private LocalDateTime updateTime;


    @TableField(exist = false)
    private Double distance;
}
