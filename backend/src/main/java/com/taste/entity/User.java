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
 * @since 2025-04-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Phone number
     */
    private String phone;

    /**
     * Password, Save encrypted
     */
    private String password;

    /**
     * Nick name, default value generated as random characters
     */
    private String nickName;

    /**
     * User icon
     */
    private String icon = "";

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
