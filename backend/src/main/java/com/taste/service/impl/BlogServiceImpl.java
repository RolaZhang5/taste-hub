package com.taste.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taste.dto.Result;
import com.taste.dto.ScrollResult;
import com.taste.dto.UserDTO;
import com.taste.entity.Blog;
import com.taste.entity.Follow;
import com.taste.entity.User;
import com.taste.mapper.BlogMapper;
import com.taste.service.IBlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taste.service.IFollowService;
import com.taste.service.IUserService;
import com.taste.utils.SystemConstants;
import com.taste.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.taste.utils.RedisConstants.BLOG_LIKED_KEY;
import static com.taste.utils.RedisConstants.FEED_KEY;

/**
 * <p>
 * Service implementation class 
 * </p>
 *
 * @author
 * @since 2025-04-23
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {
    @Resource
    private IUserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IFollowService followService;

    @Override
    public Result queryHotBlog(Integer current) {
        // Query data with pagination, ordered by number of likes
        Page<Blog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // Retrieve data for the  current pagination page
        List<Blog> records = page.getRecords();
        // Query user
        records.forEach(blog -> {
            this.queryBlogUser(blog);
            this.isBlogLiked(blog);
        });
        return Result.ok(records);
    }

    @Override
    public Result queryBlogById(Long id) {
        Blog blog = getById(id);
        if (blog == null) {
            return Result.fail("blog does not exist!");
        }
        queryBlogUser(blog);
//        check weather the current user already liked the blog
        isBlogLiked(blog);
        return Result.ok(blog);
    }

    private void isBlogLiked(Blog blog) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
//            user not logged in, skip query and retun
            return;
        }
        Long userId = user.getId();
        String key = BLOG_LIKED_KEY + blog.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        blog.setIsLike(score != null);
    }

    @Override
    public Result likeBlog(Long id) {
//        1.Get the current user
        Long userId = UserHolder.getUser().getId();
//        2.Check whether the current user has liked the blog
        String key = BLOG_LIKED_KEY + id;
//        Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        if (score == null) {
            //        3. if not liked, allow the user to like the blog
//        3.1 Increment the blog's like count by 1
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
//        3.2 add the user to the redis set
            if (isSuccess) {
                //        save user info to redis zset, zadd key value score
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }
        } else {
            //        4.if already liked, allow the user to cancle the like
            //        4.1 Decrease the blog's like count by 1
            boolean isSuccess = update().setSql("liked = liked - 1").eq("id", id).update();
//        4.2 Remove user from the Redis set
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }
        }
        return Result.ok();
    }

    @Override
    public Result queryBlogLikes(Long id) {
//         1.Get the top 5 users who liked the blog, ordered by time, zrange key 0 4
        String key = BLOG_LIKED_KEY + id;
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if (top5 == null || top5.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
//        2. Extract user ids from the set
        List<Long> ids = top5.stream().map(Long::valueOf).collect(Collectors.toList());
//        3.Query user info based on user ids WHERE id IN (5, 1) ORDER BY FIELD(id, 5, 1)
        String idStr = StrUtil.join(",", ids);
        List<UserDTO> userDTOS = userService.query().in("id", ids).last("ORDER BY FIELD( id," + idStr + ")").list()
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class)).collect(Collectors.toList());
//        4.Return the result
        return Result.ok(userDTOS);
    }

    @Override
    public Result saveBlog(Blog blog) {
        // get log in user
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        // save blog
        boolean isSuccess = save(blog);
        if (!isSuccess) {
            return Result.fail("add blog fail!");
        }
//        get all fans
        List<Follow> followers = followService.query().eq("follow_user_id", user.getId()).list();
        for (Follow follower : followers) {
//            push to followers
            String key = FEED_KEY + follower.getUserId();
            stringRedisTemplate.opsForZSet().add(key, blog.getId().toString(), System.currentTimeMillis());
        }
        // return id
        return Result.ok();
    }

    @Override
    public Result queryBlogOfFollow(Long max, Integer offset) {
//        1. get the current user info
        UserDTO user = UserHolder.getUser();
//        2.check inbox (redis feed)  zrevrange key min max withscores limit offset count
        String key = FEED_KEY + user.getId();
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, 0, max, offset, 2);
//         3.Check if not null
        if (typedTuples == null || typedTuples.isEmpty()) {
            return Result.ok();
        }
        //        4.extract feed info, blogId, minTime, offset
        List<Long> ids = new ArrayList<>();
        long minTime = 0;
        int os = 1;
        for (ZSetOperations.TypedTuple<String> tuple : typedTuples) { // 5 4 4 2 2
//            4.1 get blog id
            String idStr = tuple.getValue();
            ids.add(Long.valueOf(idStr));
//            4.2 get score(timestamp)
            long time = tuple.getScore().longValue();
            if (time == minTime)
                os++;
            else{
                minTime = time;
                os = 1;
            }
        }
//        5.query blog info by blogId
        String idStr = StrUtil.join(",", ids);
        List<Blog> blogs = query().in("id", ids).last("order by field(id," + idStr + ")").list();
        blogs.forEach(blog -> {
//            5.1 query blog author
            this.queryBlogUser(blog);
//            5.2 query if the blog is liked
            this.isBlogLiked(blog);
        });
        //        5.wrap and return
        ScrollResult r = new ScrollResult();
        r.setList(blogs);
        r.setMinTime(minTime);
        r.setOffset(os);
        return Result.ok(r);
    }

    private void queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }

}
