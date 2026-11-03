package com.travel.common.utils;

/**
 * Redis 缓存 key 与过期时间常量。
 * <p>
 * key 设计约定：
 * <ul>
 *   <li>列表缓存带查询参数（{@code *:list:{current}:{size}:{name}:{city}:{categoryId}}），
 *       便于精确命中；失效时按前缀 {@code *:list:*} 批量删除。</li>
 *   <li>详情缓存拆分为「实体」与「子资源」两个 key，各自独立失效。</li>
 *   <li>推荐结果按用户维度缓存，跟随用户行为变化。</li>
 * </ul>
 */
public final class CacheConstants {

    private CacheConstants() {
    }

    /** 列表缓存过期：10 分钟 */
    public static final long LIST_TTL = 600L;
    /** 详情缓存过期：30 分钟 */
    public static final long DETAIL_TTL = 1800L;
    /** 推荐结果缓存过期：5 分钟 */
    public static final long RECOMMEND_TTL = 300L;

    // ---------- 公共内容 ----------
    public static final String BANNER_LIST = "banner:list";
    public static final String NOTICE_LIST = "notice:list";

    // ---------- 酒店 ----------
    public static final String HOTEL_LIST = "hotel:list:";
    public static final String HOTEL_DETAIL = "hotel:detail:";
    public static final String HOTEL_ROOMS = "hotel:rooms:";

    // ---------- 景点 ----------
    public static final String SPOT_LIST = "spot:list:";
    public static final String SPOT_DETAIL = "spot:detail:";
    public static final String SPOT_TICKETS = "spot:tickets:";

    // ---------- 餐厅 ----------
    public static final String RESTAURANT_LIST = "restaurant:list:";
    public static final String RESTAURANT_DETAIL = "restaurant:detail:";
    public static final String RESTAURANT_DISHES = "restaurant:dishes:";

    // ---------- 推荐 ----------
    public static final String RECOMMEND = "recommend:user:";
}
