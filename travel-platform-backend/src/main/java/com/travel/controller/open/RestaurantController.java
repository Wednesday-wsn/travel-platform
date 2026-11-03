package com.travel.controller.open;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.travel.common.exception.BusinessException;
import com.travel.common.result.R;
import com.travel.common.result.ResultCode;
import com.travel.common.utils.CacheConstants;
import com.travel.common.utils.RedisUtil;
import com.travel.entity.BizDish;
import com.travel.entity.BizRestaurant;
import com.travel.service.DishService;
import com.travel.service.RestaurantService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 公开接口：餐厅浏览（列表与详情接入 Redis 读穿缓存）
 */
@RestController
@RequestMapping("/api/public/restaurant")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final DishService dishService;
    private final RedisUtil redisUtil;

    public RestaurantController(RestaurantService restaurantService, DishService dishService, RedisUtil redisUtil) {
        this.restaurantService = restaurantService;
        this.dishService = dishService;
        this.redisUtil = redisUtil;
    }

    @GetMapping("/page")
    public R<Page<BizRestaurant>> page(@RequestParam(defaultValue = "1") long current,
                                       @RequestParam(defaultValue = "10") long size,
                                       @RequestParam(required = false) String name,
                                       @RequestParam(required = false) String city,
                                       @RequestParam(required = false) Long categoryId) {
        String key = CacheConstants.RESTAURANT_LIST + current + ":" + size + ":"
                + (name == null ? "" : name) + ":"
                + (city == null ? "" : city) + ":"
                + (categoryId == null ? "" : categoryId);
        Page<BizRestaurant> cached = redisUtil.get(key, new TypeReference<Page<BizRestaurant>>() {
        });
        if (cached != null) {
            return R.ok(cached);
        }

        LambdaQueryWrapper<BizRestaurant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizRestaurant::getStatus, 1);
        if (StringUtils.hasText(name)) {
            wrapper.like(BizRestaurant::getName, name);
        }
        if (StringUtils.hasText(city)) {
            wrapper.eq(BizRestaurant::getCity, city);
        }
        if (categoryId != null) {
            wrapper.eq(BizRestaurant::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(BizRestaurant::getCreateTime);
        Page<BizRestaurant> result = restaurantService.page(new Page<>(current, size), wrapper);
        redisUtil.set(key, result, CacheConstants.LIST_TTL, TimeUnit.SECONDS);
        return R.ok(result);
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        BizRestaurant restaurant = redisUtil.get(CacheConstants.RESTAURANT_DETAIL + id, BizRestaurant.class);
        if (restaurant == null) {
            restaurant = restaurantService.getById(id);
            if (restaurant != null && restaurant.getStatus() == 1) {
                redisUtil.set(CacheConstants.RESTAURANT_DETAIL + id, restaurant, CacheConstants.DETAIL_TTL, TimeUnit.SECONDS);
            }
        }
        if (restaurant == null || restaurant.getStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        List<BizDish> dishes = redisUtil.get(CacheConstants.RESTAURANT_DISHES + id, new TypeReference<List<BizDish>>() {
        });
        if (dishes == null) {
            dishes = dishService.lambdaQuery()
                    .eq(BizDish::getRestaurantId, id)
                    .eq(BizDish::getStatus, 1)
                    .list();
            redisUtil.set(CacheConstants.RESTAURANT_DISHES + id, dishes, CacheConstants.DETAIL_TTL, TimeUnit.SECONDS);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("info", restaurant);
        data.put("dishes", dishes);
        return R.ok(data);
    }
}
