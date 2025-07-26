package com.taste.service;

import com.taste.dto.Result;
import com.taste.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  Service class
 * </p>
 *
 * @author RolaZhang
 * @since 2025-04-15
 */
public interface IShopTypeService extends IService<ShopType> {

    Result queryShopType();
}
