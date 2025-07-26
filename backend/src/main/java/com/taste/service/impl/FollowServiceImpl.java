package com.taste.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taste.dto.Result;
import com.taste.dto.UserDTO;
import com.taste.entity.Follow;
import com.taste.mapper.FollowMapper;
import com.taste.service.IFollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taste.service.IUserService;
import com.taste.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * Service implementation class 
 * </p>
 *
 * @author
 * @since 2025-05-03
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IUserService userService;
    @Override
    public Result follow(Long followUserId) {
// check whether the user follow or not
        UserDTO user = UserHolder.getUser();
        if (user == null)
            return Result.ok(false);
        Integer count = query().eq("user_id", user.getId()).eq("follow_user_id", followUserId).count();
        return Result.ok(count > 0);
    }

    @Override
    public Result follow(Long followUserId, Boolean isFollow) {
        UserDTO user = UserHolder.getUser();
        if (user == null)
            return Result.fail("please log in to your account!");
        String key = "follows:" + user.getId();
        //        1.check whether the user wants to follow or unfollow
        if (isFollow) {
            //        2. if the user wants to follow than create the data
            Follow follow = new Follow();
            follow.setUserId(user.getId());
            follow.setFollowUserId(followUserId);
            boolean isSuccess = save(follow);

            if (isSuccess) {
                stringRedisTemplate.opsForSet().add(key, followUserId.toString());
            }
        } else {
//            unfollow, delete from tb_follow where user_id = ? and follow_user_id = ?
//            3. if the user wants to unfollow,delete the data
            boolean isSuccess = remove(new QueryWrapper<Follow>().eq("user_id", user.getId()).eq("follow_user_id", followUserId));
            if (isSuccess)
                stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
        }
        return Result.ok();
    }

    //serch followCommons
    @Override
    public Result followCommons(Long id) {
        UserDTO user = UserHolder.getUser();
        if (user == null)
            return Result.fail("please log in to your account!");
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect("follows:" + user.getId().toString(), "follows:" + id.toString());
        if(intersect == null || intersect.isEmpty()){
            return Result.ok();
        }
//        extract ids
        List<Long> userIds = intersect.stream().map(Long::valueOf).collect(Collectors.toList());
        List<UserDTO> users = userService.listByIds(userIds).stream().map(user1 -> BeanUtil.copyProperties(user1, UserDTO.class)).collect(Collectors.toList());
        return Result.ok(users);
    }
}
