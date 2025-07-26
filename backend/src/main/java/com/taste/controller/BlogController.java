package com.taste.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taste.dto.Result;
import com.taste.dto.UserDTO;
import com.taste.entity.Blog;
import com.taste.service.IBlogService;
import com.taste.service.IUserService;
import com.taste.utils.SystemConstants;
import com.taste.utils.UserHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 *
 * @author RolaZhang
 * @since 2025-04-23
 */
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private IBlogService blogService;
    @Resource
    private IUserService userService;
    private Long userId;

//    @PostMapping
//    public Result saveBlog(@RequestBody Blog blog) {
//        // Get login user
//        UserDTO user = UserHolder.getUser();
//        blog.setUserId(user.getId());
//        // Save shop review post
//        blogService.save(blog);
//        // Return id
//        return Result.ok(blog.getId());
//    }

    @PutMapping("/like/{id}")
    public Result likeBlog(@PathVariable("id") Long id) {
        return blogService.likeBlog(id);
    }

    @GetMapping("/of/me")
    public Result queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // Get login user
        UserDTO user = UserHolder.getUser();
        // Query by user
        Page<Blog> page = blogService.query()
                .eq("user_id", user.getId()).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // Get current-page data
        List<Blog> records = page.getRecords();
        return Result.ok(records);
    }

    @GetMapping("/hot")
    public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
       return blogService.queryHotBlog(current);
    }

    @GetMapping("/{id}")
    public Result queryBlogById(@PathVariable("id") Long id){
        return blogService.queryBlogById(id);
    }

    @GetMapping("/likes/{id}")
    public Result queryBlogLikes(@PathVariable("id") Long id){
        return blogService.queryBlogLikes(id);
    }

    @GetMapping("/of/user")
    public Result queryBlogByUserId(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam("id") Long id) {
        // Query by user
        Page<Blog> page = blogService.query()
                .eq("user_id", id).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // Get current-page data
        List<Blog> records = page.getRecords();
        return Result.ok(records);
    }
    @PostMapping
    public Result saveBlog(@RequestBody Blog blog) {
        return blogService.saveBlog(blog);
    }

    @GetMapping("/of/follow")
    public Result queryBlogOfFollow(@RequestParam("lastId") Long max, @RequestParam(value = "offset", defaultValue = "0") Integer offset){
        return blogService.queryBlogOfFollow(max, offset);
    }
}
