package com.travel.controller.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.context.UserContext;
import com.travel.common.exception.BusinessException;
import com.travel.common.result.R;
import com.travel.common.result.ResultCode;
import com.travel.common.utils.CacheEvictor;
import com.travel.entity.BizDish;
import com.travel.entity.BizRestaurant;
import com.travel.service.DishService;
import com.travel.service.RestaurantService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商家端：餐厅与菜品管理
 */
@RestController
@RequestMapping("/api/merchant/restaurant")
public class MerchantRestaurantController {

    private final RestaurantService restaurantService;
    private final DishService dishService;
    private final CacheEvictor cacheEvictor;

    public MerchantRestaurantController(RestaurantService restaurantService, DishService dishService, CacheEvictor cacheEvictor) {
        this.restaurantService = restaurantService;
        this.dishService = dishService;
        this.cacheEvictor = cacheEvictor;
    }

    // ---------- 餐厅 ----------

    @GetMapping("/page")
    public R<Page<BizRestaurant>> page(@RequestParam(defaultValue = "1") long current,
                                       @RequestParam(defaultValue = "10") long size,
                                       @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<BizRestaurant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizRestaurant::getMerchantId, UserContext.getUserId());
        if (status != null) {
            wrapper.eq(BizRestaurant::getStatus, status);
        }
        wrapper.orderByDesc(BizRestaurant::getCreateTime);
        return R.ok(restaurantService.page(new Page<>(current, size), wrapper));
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        BizRestaurant restaurant = checkOwnRestaurant(id);
        List<BizDish> dishes = dishService.lambdaQuery()
                .eq(BizDish::getRestaurantId, id).list();
        Map<String, Object> data = new HashMap<>();
        data.put("info", restaurant);
        data.put("dishes", dishes);
        return R.ok(data);
    }

    @PostMapping
    public R<Void> create(@RequestBody BizRestaurant restaurant) {
        restaurant.setId(null);
        restaurant.setMerchantId(UserContext.getUserId());
        restaurant.setStatus(0); // 待审核
        restaurant.setRating(java.math.BigDecimal.valueOf(5.0));
        restaurant.setCommentCount(0);
        restaurantService.save(restaurant);
        return R.ok();
    }

    @PutMapping
    public R<Void> update(@RequestBody BizRestaurant restaurant) {
        checkOwnRestaurant(restaurant.getId());
        restaurant.setMerchantId(UserContext.getUserId());
        restaurant.setStatus(0); // 修改后重新审核
        restaurantService.updateById(restaurant);
        cacheEvictor.evictRestaurant(restaurant.getId());
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        checkOwnRestaurant(id);
        restaurantService.removeById(id);
        cacheEvictor.evictRestaurant(id);
        return R.ok();
    }

    @PutMapping("/{id}/shelf")
    public R<Void> shelf(@PathVariable Long id) {
        BizRestaurant restaurant = checkOwnRestaurant(id);
        restaurant.setStatus(0); // 申请上架：重新进入待审核
        restaurantService.updateById(restaurant);
        cacheEvictor.evictRestaurant(id);
        return R.ok();
    }

    @PutMapping("/{id}/off")
    public R<Void> off(@PathVariable Long id) {
        BizRestaurant restaurant = checkOwnRestaurant(id);
        restaurant.setStatus(3); // 商家主动下架
        restaurantService.updateById(restaurant);
        cacheEvictor.evictRestaurant(id);
        return R.ok();
    }

    // ---------- 菜品 ----------

    @GetMapping("/dish/list")
    public R<List<BizDish>> dishList(@RequestParam Long restaurantId) {
        checkOwnRestaurant(restaurantId);
        return R.ok(dishService.lambdaQuery()
                .eq(BizDish::getRestaurantId, restaurantId).list());
    }

    @PostMapping("/dish")
    public R<Void> createDish(@RequestBody BizDish dish) {
        checkOwnRestaurant(dish.getRestaurantId());
        dish.setId(null);
        dish.setStatus(1);
        dish.setSales(0);
        dishService.save(dish);
        cacheEvictor.evictRestaurant(dish.getRestaurantId());
        return R.ok();
    }

    @PutMapping("/dish")
    public R<Void> updateDish(@RequestBody BizDish dish) {
        BizDish exist = dishService.getById(dish.getId());
        if (exist == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        checkOwnRestaurant(exist.getRestaurantId());
        dishService.updateById(dish);
        cacheEvictor.evictRestaurant(exist.getRestaurantId());
        return R.ok();
    }

    @DeleteMapping("/dish/{id}")
    public R<Void> deleteDish(@PathVariable Long id) {
        BizDish dish = dishService.getById(id);
        if (dish != null) {
            checkOwnRestaurant(dish.getRestaurantId());
            dishService.removeById(id);
            cacheEvictor.evictRestaurant(dish.getRestaurantId());
        }
        return R.ok();
    }

    private BizRestaurant checkOwnRestaurant(Long restaurantId) {
        BizRestaurant restaurant = restaurantService.getById(restaurantId);
        if (restaurant == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!restaurant.getMerchantId().equals(UserContext.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return restaurant;
    }
}
