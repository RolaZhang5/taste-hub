package com.taste.service;

import com.taste.dto.Result;
import com.taste.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  Service class
 * </p>
 *
 * @author RolaZhang
 * @since 2025-04-18
 */
public interface IShopService extends IService<Shop> {

    Result queryById(Long id);


    Result update(Shop shop);

    Result queryShopByType(Integer typeId, Integer current, Double x, Double y);
}
