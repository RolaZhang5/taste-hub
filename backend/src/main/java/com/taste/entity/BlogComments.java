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
 * <p>
 * 
 * </p>
 *
 * @author RolaZhang
 * @since 2025-04-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_blog_comments")
public class BlogComments implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * User id
     */
    private Long userId;

    /**
     * Blog id
     */
    private Long blogId;

    /**
     * ID of the associated first-level comment; 0 if this comment is first-level
     */
    private Long parentId;

    /**
     *  Reply comment ID
     */
    private Long answerId;

    /**
     * Reply comment content
     */
    private String content;

    /**
     * Likes
     */
    private Integer liked;

    /**
     * status: 0: Normal, 1: Reported, 2: Forbidden to view
     */
    private Boolean status;

    /**
     * Creation time
     */
    private LocalDateTime createTime;

    /**
     * Update time
     */
    private LocalDateTime updateTime;


}
