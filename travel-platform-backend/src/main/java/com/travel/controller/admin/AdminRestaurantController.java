package com.travel.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.exception.BusinessException;
import com.travel.common.result.R;
import com.travel.common.result.ResultCode;
import com.travel.common.utils.CacheEvictor;
import com.travel.entity.BizRestaurant;
import com.travel.service.RestaurantService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端：餐厅监管
 */
@RestController
@RequestMapping("/api/admin/restaurant")
public class AdminRestaurantController {

    private final RestaurantService restaurantService;
    private final CacheEvictor cacheEvictor;

    public AdminRestaurantController(RestaurantService restaurantService, CacheEvictor cacheEvictor) {
        this.restaurantService = restaurantService;
        this.cacheEvictor = cacheEvictor;
    }

    @GetMapping("/page")
    public R<Page<BizRestaurant>> page(@RequestParam(defaultValue = "1") long current,
                                       @RequestParam(defaultValue = "10") long size,
                                       @RequestParam(required = false) Integer status,
                                       @RequestParam(required = false) String name) {
        LambdaQueryWrapper<BizRestaurant> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(BizRestaurant::getStatus, status);
        }
        if (StringUtils.hasText(name)) {
            wrapper.like(BizRestaurant::getName, name);
        }
        wrapper.orderByAsc(BizRestaurant::getId);
        return R.ok(restaurantService.page(new Page<>(current, size), wrapper));
    }

    @PutMapping("/{id}/shelf")
    public R<Void> shelf(@PathVariable Long id) {
        BizRestaurant restaurant = restaurantService.getById(id);
        if (restaurant == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        restaurant.setStatus(1);
        restaurantService.updateById(restaurant);
        cacheEvictor.evictRestaurant(id);
        return R.ok();
    }

    @PutMapping("/{id}/off")
    public R<Void> off(@PathVariable Long id) {
        BizRestaurant restaurant = restaurantService.getById(id);
        if (restaurant == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        restaurant.setStatus(3);
        restaurantService.updateById(restaurant);
        cacheEvictor.evictRestaurant(id);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        restaurantService.removeById(id);
        cacheEvictor.evictRestaurant(id);
        return R.ok();
    }
}
