package com.taste.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taste.dto.Result;
import com.taste.entity.ShopType;
import com.taste.mapper.ShopTypeMapper;
import com.taste.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.taste.utils.RedisConstants.CACHE_SHOP_TTL;

/**
 * <p>
 *  Service implementation class 
 * </p>
 *
 * @author RolaZhang
 * @since 2025-04-15
 */
@Slf4j
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryShopType() {
        //        1. Query from redis and check
        String key = "cache:shop-type:";
        String jsonShopType = stringRedisTemplate.opsForValue().get(key);
//        2. Return the result if it exists
        if(!StrUtil.isBlank(jsonShopType)){
            ObjectMapper mapper = new ObjectMapper();
            try {
                List<ShopType> listObj = mapper.readValue(jsonShopType, List.class);
                return Result.ok(listObj);
            } catch (JsonProcessingException e) {
                log.error("Faild to query shop type, reason: {}", e.toString());
            }
            return Result.fail("Faild to query shop type!");
        }
//        3. If not found in cache, query from the database
        List<ShopType> shopType = query().orderByAsc("sort").list();
//        4. If not found, return error
        if(shopType.size() == 0){
            return Result.fail("Faild to query shop type form database!");
        }
//        5. If found, return the result and save it in cache
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shopType), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        return Result.ok(shopType);
    }
}
