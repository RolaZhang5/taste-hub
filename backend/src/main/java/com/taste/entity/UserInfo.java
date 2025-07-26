package com.taste.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author RolaZhang
 * @since 2025-04-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_user_info")
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key, user ID
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**
     * city
     */
    private String city;

    /**
     * Personal introduction, max length 128 characters
     */
    private String introduce;

    /**
     * Number of fans
     */
    private Integer fans;

    /**
     * Number Of followers
     */
    private Integer follower;

    /**
     * Gender: 0: male, 1: female
     */
    private Boolean gender;

    /**
     * Birthday
     */
    private LocalDate birthday;

    /**
     * Credit points
     */
    private Integer credits;

    /**
     * Member level(0-9), where 0 means not a member 会员级别，0~9级,0代表未开通会员
     */
    private Boolean level;


    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
