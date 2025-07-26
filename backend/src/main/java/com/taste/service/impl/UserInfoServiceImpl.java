package com.taste.service.impl;

import com.taste.entity.UserInfo;
import com.taste.mapper.UserInfoMapper;
import com.taste.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  Service implementation class 
 * </p>
 *
 * @author RolaZhang
 * @since 2025-04-10
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

}
