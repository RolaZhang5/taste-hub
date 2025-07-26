package com.taste.service;

import com.taste.dto.Result;
import com.taste.entity.Follow;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  Service class
 * </p>
 *
 * @author RolaZhang
 * @since 2025-05-03
 */
public interface IFollowService extends IService<Follow> {

    Result follow(Long followUserId);

    Result follow(Long followUserId, Boolean isFollow);

    Result followCommons(Long id);
}
