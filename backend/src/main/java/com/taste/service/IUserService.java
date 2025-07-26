package com.taste.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taste.dto.LoginFormDTO;
import com.taste.dto.Result;
import com.taste.entity.User;

import javax.servlet.http.HttpSession;

/**
 * <p>
 *  Service class
 * </p>
 *
 * @author RolaZhang
 * @since 2025-04-10
 */
public interface IUserService extends IService<User> {

    Result send(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);

    Result sign();

    Result signCount();

    Result logout();
}
