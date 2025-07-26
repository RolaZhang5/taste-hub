package com.taste.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taste.dto.LoginFormDTO;
import com.taste.dto.Result;
import com.taste.dto.UserDTO;
import com.taste.entity.User;
import com.taste.mapper.UserMapper;
import com.taste.service.IUserService;
import com.taste.utils.RegexUtils;
import com.taste.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.taste.utils.RedisConstants.*;
import static com.taste.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * Service implementation class 
 * </p>
 *
 * @author Rola
 * @since 2025-04-10
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public Result send(String phone, HttpSession session) {
//        1. Validate the phone number
        if (RegexUtils.isPhoneInvalid(phone)) {
            //        2. Return error If the format does not match
            return Result.fail("Invalid phone number format!");
        }
//        3. Create a validation code if match
        String code = RandomUtil.randomNumbers(6);
////        4. Save the validation code to the session
//        session.setAttribute("code", code);
        //        4. Save the validation code to redis
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
//        5. Send the validation code
        log.debug("validation code message sent successfully, code: {}", code);
//        6. Return ok
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        //1. Validate phone number and code
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("Invalid phone formate！");
        }
//        2. Retrieve the validation code from the session
//        Object cacheCode = session.getAttribute("code");
//        2. Fetch the validation code from redis
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            //        3.Throw an error if does not match
            return Result.fail("Invalid verification code!");
        }
//        4. Query user by phone number if it matches tb_user
        User user = query().eq("phone", phone).one();
        //        5. Check if the user exists 判断用户是否存在
        if (user == null) {
            //        6. Create a new user if they do not exist
            user = createUserWithPhone(phone);
        }
////        7. Save user infomation to the session
//        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        //        7. Save user infomation to redis
//        7.1 Create a random token for login authentication
        String token = UUID.randomUUID().toString();
//        7.2 Save the user object into Redis as hash
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
//        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(), CopyOptions.create().setIgnoreNullValue(true).setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
//        7.3 Save
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
//        7.4 Set expiration time
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);
        return Result.ok(token);
    }

    @Override
    public Result logout() {
        //        1. Get the token from request header
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes())
                .getRequest();
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)){
            return Result.ok("Successfully logged out！");
        }
        String key = LOGIN_USER_KEY + token;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);

        if (userMap.isEmpty()) {
            return Result.ok("Successfully logged out！");
        }
        if (token != null) {
            long expiration = stringRedisTemplate.getExpire(key,TimeUnit.MILLISECONDS);
            stringRedisTemplate.opsForValue().set(key, "{}", expiration, TimeUnit.MILLISECONDS);
        }
        return Result.ok("Successfully logged out！");
    }

    @Override
    public Result sign() {
        // 1.Get the current user info
        Long userId = UserHolder.getUser().getId();
//        2.Get the date
        LocalDateTime now = LocalDateTime.now();
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        //        3.Concatenate key
        String key = USER_SIGN_KEY + userId.toString() + keySuffix;
//        4.Get what is today's day of the month
        int dayOfMonth = now.getDayOfMonth();
//        5.Write to Redis, SETBIT key offset 1
        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
        return Result.ok();
    }

    @Override
    public Result signCount() {
        // 1.Get the current user info
        Long userId = UserHolder.getUser().getId();
//        2.Get the date
        LocalDateTime now = LocalDateTime.now();
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        //        3.Concatenate key
        String key = USER_SIGN_KEY + userId.toString() + keySuffix;
//        4.Get what is today's day of the month
        int dayOfMonth = now.getDayOfMonth();
//        5.Get the all sign record till today, return number in decimal; BITFIELD sign:1010:202506 GET u8 0
        List<Long> result = stringRedisTemplate.opsForValue().bitField(key, BitFieldSubCommands.create().get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0));
        if (result == null||result.isEmpty()) {
//            No signed result
            return Result.ok(0);
        }
        Long num = result.get(0);
        if (num == null||num == 0) {
            //            No signed result
            return Result.ok(0);
        }
//       6.iterate over
        int count = 0;
        while (true){
            //        7. Use umber & 1, get the last bit
            if ((num&1) == 0) {
                //        8. Check whether the current bit is 0
                break;
            }else {
                //        9. If it's not 0, it means the number is signed, increment the counter by 1
                count++;
            }
//        10.Shift the number one bit to the right, dropping the least significant bit, and continue to the next bit.
            num >>>= 1;
        }
        return Result.ok(count);
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        user.setPhone(phone);
        save(user);
        return user;
    }

}
