package com.travel.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.context.UserContext;
import com.travel.common.exception.BusinessException;
import com.travel.common.result.R;
import com.travel.common.utils.CacheEvictor;
import com.travel.entity.BizFavorite;
import com.travel.service.FavoriteService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端：收藏
 */
@RestController
@RequestMapping("/api/user/favorite")
public class UserFavoriteController {

    private final FavoriteService favoriteService;
    private final CacheEvictor cacheEvictor;

    public UserFavoriteController(FavoriteService favoriteService, CacheEvictor cacheEvictor) {
        this.favoriteService = favoriteService;
        this.cacheEvictor = cacheEvictor;
    }

    @PostMapping
    public R<Void> add(@RequestBody BizFavorite favorite) {
        Long userId = UserContext.getUserId();
        Long count = favoriteService.lambdaQuery()
                .eq(BizFavorite::getUserId, userId)
                .eq(BizFavorite::getTargetType, favorite.getTargetType())
                .eq(BizFavorite::getTargetId, favorite.getTargetId())
                .count();
        if (count != null && count > 0) {
            throw new BusinessException("已收藏，请勿重复收藏");
        }
        favorite.setUserId(userId);
        favoriteService.save(favorite);
        cacheEvictor.evictRecommend(userId);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        BizFavorite favorite = favoriteService.getById(id);
        if (favorite != null && favorite.getUserId().equals(UserContext.getUserId())) {
            favoriteService.removeById(id);
            cacheEvictor.evictRecommend(UserContext.getUserId());
        }
        return R.ok();
    }

    @GetMapping("/page")
    public R<Page<BizFavorite>> page(@RequestParam(defaultValue = "1") long current,
                                     @RequestParam(defaultValue = "10") long size,
                                     @RequestParam(required = false) Integer targetType) {
        LambdaQueryWrapper<BizFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizFavorite::getUserId, UserContext.getUserId());
        if (targetType != null) {
            wrapper.eq(BizFavorite::getTargetType, targetType);
        }
        wrapper.orderByDesc(BizFavorite::getCreateTime);
        return R.ok(favoriteService.page(new Page<>(current, size), wrapper));
    }
}
