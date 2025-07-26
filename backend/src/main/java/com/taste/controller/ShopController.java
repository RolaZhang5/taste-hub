package com.taste.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taste.dto.Result;
import com.taste.entity.Shop;
import com.taste.service.IShopService;
import com.taste.utils.SystemConstants;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**

 *
 * @author RolaZhang
 * @since 2025-04-17
 */
@RestController
@RequestMapping("/shop")
public class ShopController {

    @Resource
    public IShopService shopService;

    /**
     * Query shop information by ID
     * @param id Shop ID
     * @return Shop detail data
     */
    @GetMapping("/{id}")
    public Result queryShopById(@PathVariable("id") Long id) {
        return shopService.queryById(id);
    }

    /**
     * Add a new shop
     * @param shop shop detail
     * @return shop id
     */
    @PostMapping
    public Result saveShop(@RequestBody Shop shop) {
        // Write to the database
        shopService.save(shop);
        // Return the Shop ID
        return Result.ok(shop.getId());
    }

    /**
     * Updates shop info
     * @param shop Shop info
     * @return None
     */
    @PutMapping
    public Result updateShop(@RequestBody Shop shop) {
        // Write to the database
        return shopService.update(shop);
    }

    /**
     * Queries shop infomation by shop type with pagination
     * @param typeId The ID of the Shop type
     * @param current The current page number
     * @return A list of shops
     */
    @GetMapping("/of/type")
    public Result queryShopByType(
            @RequestParam("typeId") Integer typeId,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "x", required = false) Double x,
            @RequestParam(value = "y", required = false) Double y
    ) {
        return shopService.queryShopByType(typeId, current, x, y);
    }

    /**
     * Queries shop infomation by shop name keyword with pagination
     * @param name The shop name keyword
     * @param current The current page number
     * @return A list of shop infomation
     */
    @GetMapping("/of/name")
    public Result queryShopByName(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        // Queries types with pagination
        Page<Shop> page = shopService.query()
                .like(StrUtil.isNotBlank(name), "name", name)
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // Return the data
        return Result.ok(page.getRecords());
    }
}
