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
 * @since 2025-04-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_blog")
public class Blog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primaty key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * Shop id
     */
    private Long shopId;
    /**
     * User id
     */
    private Long userId;
    /**
     * User icon
     */
    @TableField(exist = false)
    private String icon;
    /**
     * User name
     */
    @TableField(exist = false)
    private String name;
    /**
     * Check if the user has liked it
     */
    @TableField(exist = false)
    private Boolean isLike;

    /**
     * Titile
     */
    private String title;

    /**
     * Maximum 9 images allowed for shop visit. Sepatate upload address with ";"
     */
    private String images;

    /**
     * Shop visit text content
     */
    private String content;

    /**
     * Number Of likes
     */
    private Integer liked;

    /**
     * Number of comments
     */
    private Integer comments;

    /**
     * Creation time
     */
    private LocalDateTime createTime;

    /**
     * Update time
     */
    private LocalDateTime updateTime;


}
