package com.taste.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.taste.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.taste.utils.RedisConstants.LOGIN_USER_KEY;
import static com.taste.utils.RedisConstants.LOGIN_USER_TTL;

public class RefreshTokenIntercepter implements HandlerInterceptor {
    private StringRedisTemplate stringRedisTemplate;

    public RefreshTokenIntercepter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
////        1. Fetch the session
//        HttpSession session = request.getSession();

//        //2. Fetch the user infomation from the session
//        Object user = session.getAttribute("user");
//        //3. Check if the user exists
//        if (user == null) {
//            //4. if the user deos not exist, intercept, return 401 Unauthorized
//            response.setStatus(401);
//            return false;
//        }
//        //5. Save the user infomation to ThreadLocal if it exists
////        UserHolder.saveUser((UserDTO) user);

//        1. Fetch the token from the request header
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)){
            return true;
        }
//        2. Fetch the user infomation from Redis based on token and convert the  retrieved hash data to UserDTO object
//        UserDTO userDTO = (UserDTO) stringRedisTemplate.opsForHash().get(token, UserDTO.class);
        String key = LOGIN_USER_KEY + token;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);

        if (userMap.isEmpty()) {
            return true;
        }
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
//        6. Save the infomation to threadLocal if it exists
        UserHolder.saveUser(userDTO);
//        7. Refresh the token expiration time 刷新token有效期
        stringRedisTemplate.expire(key, LOGIN_USER_TTL, TimeUnit.MINUTES);
        //6. Proceed
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
