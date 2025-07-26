package com.taste.controller;


import cn.hutool.core.bean.BeanUtil;
import com.taste.dto.LoginFormDTO;
import com.taste.dto.Result;
import com.taste.dto.UserDTO;
import com.taste.entity.User;
import com.taste.entity.UserInfo;
import com.taste.service.IUserInfoService;
import com.taste.service.IUserService;
import com.taste.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**

 *
 * @author Rola
 * @since 2025-04-10
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    /**
     * Send the phone code
     */
    @PostMapping("code")
    public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
        // Send the phone code and save the verification code
        return userService.send(phone, session);
    }

    /**
     * Sign-in functionality
     * @param loginForm login param，include phone number, verificate code; or phone number, password
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm, HttpSession session){
        // Implement login functionality
        return userService.login(loginForm, session);
    }

    /**
     * Logout functionality
     * @return None
     */
    @PostMapping("/logout")
    public Result logout(){
        return userService.logout();
    }

    @GetMapping("/me")
    public Result me(){
        // Get the currently logged-in user and return
        UserDTO user = UserHolder.getUser();
        if(user == null){
            return Result.fail("Please log in again！");
        }
        return Result.ok(user);
    }

    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId){
        // Query details
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // If there are no details, it means this is the first time checking the info.
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // Return
        return Result.ok(info);
    }
    @GetMapping("/{id}")
    public Result queryUserById(@PathVariable("id") Long userId){
        // Query details
        User user = userService.getById(userId);
        if (user == null) {
            return Result.ok();
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // Return
        return Result.ok(userDTO);
    }

    //sign
    @PostMapping("/sign")
    public Result sign(){
        return userService.sign();
    }


    //sign count
    @GetMapping("/sign/count")
    public Result signCount(){
        return userService.signCount();
    }
}
