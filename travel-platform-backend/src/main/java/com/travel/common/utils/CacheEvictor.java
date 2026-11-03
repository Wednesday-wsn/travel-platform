package com.travel.common.utils;

import org.springframework.stereotype.Component;

/**
 * 缓存失效器：数据发生变更（增删改、上下架、评分重算）时，统一清理相关缓存。
 * <p>
 * 集中管理失效逻辑，避免在业务代码里散落大量 Redis 操作，保证「缓存一致性」策略清晰：
 * 写操作后主动删除缓存（Cache Aside 旁路缓存），下次读时回源数据库并重建缓存。
 */
@Component
public class CacheEvictor {

    private final RedisUtil redisUtil;

    public CacheEvictor(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    public void evictHotel(Long id) {
        redisUtil.delete(CacheConstants.HOTEL_DETAIL + id);
        redisUtil.delete(CacheConstants.HOTEL_ROOMS + id);
        redisUtil.deleteByPattern(CacheConstants.HOTEL_LIST + "*");
    }

    public void evictSpot(Long id) {
        redisUtil.delete(CacheConstants.SPOT_DETAIL + id);
        redisUtil.delete(CacheConstants.SPOT_TICKETS + id);
        redisUtil.deleteByPattern(CacheConstants.SPOT_LIST + "*");
    }

    public void evictRestaurant(Long id) {
        redisUtil.delete(CacheConstants.RESTAURANT_DETAIL + id);
        redisUtil.delete(CacheConstants.RESTAURANT_DISHES + id);
        redisUtil.deleteByPattern(CacheConstants.RESTAURANT_LIST + "*");
    }

    public void evictBanner() {
        redisUtil.delete(CacheConstants.BANNER_LIST);
    }

    public void evictNotice() {
        redisUtil.delete(CacheConstants.NOTICE_LIST);
    }

    /** 用户行为（收藏 / 下单）变化后，失效该用户的推荐结果缓存 */
    public void evictRecommend(Long userId) {
        redisUtil.delete(CacheConstants.RECOMMEND + userId);
    }
}
