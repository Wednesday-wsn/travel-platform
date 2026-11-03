package com.travel.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.travel.common.utils.CacheConstants;
import com.travel.common.utils.RedisUtil;
import com.travel.entity.BizFavorite;
import com.travel.entity.BizHotel;
import com.travel.entity.BizOrder;
import com.travel.entity.BizRestaurant;
import com.travel.entity.BizScenicSpot;
import com.travel.service.FavoriteService;
import com.travel.service.HotelService;
import com.travel.service.OrderService;
import com.travel.service.RecommendService;
import com.travel.service.RestaurantService;
import com.travel.service.ScenicSpotService;
import com.travel.vo.RecommendVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 个性化推荐实现：三级混合推荐 + Redis 缓存。
 * <p>
 * 1. 协同过滤（Item-CF）：利用收藏共现「喜欢 A 的人也喜欢 B」；
 * 2. 内容相似：同分类 / 同城市的候选；
 * 3. 热门兜底：收藏数 + 评分，解决冷启动。
 */
@Service
public class RecommendServiceImpl implements RecommendService {

    private static final Logger log = LoggerFactory.getLogger(RecommendServiceImpl.class);
    private static final int LIMIT = 10;

    private final HotelService hotelService;
    private final ScenicSpotService scenicSpotService;
    private final RestaurantService restaurantService;
    private final FavoriteService favoriteService;
    private final OrderService orderService;
    private final RedisUtil redisUtil;

    public RecommendServiceImpl(HotelService hotelService,
                                ScenicSpotService scenicSpotService,
                                RestaurantService restaurantService,
                                FavoriteService favoriteService,
                                OrderService orderService,
                                RedisUtil redisUtil) {
        this.hotelService = hotelService;
        this.scenicSpotService = scenicSpotService;
        this.restaurantService = restaurantService;
        this.favoriteService = favoriteService;
        this.orderService = orderService;
        this.redisUtil = redisUtil;
    }

    @Override
    public List<RecommendVO.Item> recommend(Long userId) {
        if (userId == null) {
            return List.of();
        }
        // 推荐结果缓存（5 分钟），命中直接返回
        List<RecommendVO.Item> cached = redisUtil.get(CacheConstants.RECOMMEND + userId, new TypeReference<List<RecommendVO.Item>>() {
        });
        if (cached != null) {
            return cached;
        }

        List<RecommendVO.Item> result = doRecommend(userId);
        redisUtil.set(CacheConstants.RECOMMEND + userId, result, CacheConstants.RECOMMEND_TTL, TimeUnit.SECONDS);
        return result;
    }

    private List<RecommendVO.Item> doRecommend(Long userId) {
        // 1. 加载上架资源（数据量小，全量加载便于多轮比较）
        List<BizHotel> hotels = hotelService.lambdaQuery().eq(BizHotel::getStatus, 1).list();
        List<BizScenicSpot> spots = scenicSpotService.lambdaQuery().eq(BizScenicSpot::getStatus, 1).list();
        List<BizRestaurant> restaurants = restaurantService.lambdaQuery().eq(BizRestaurant::getStatus, 1).list();

        Map<Long, BizHotel> hotelMap = hotels.stream().collect(Collectors.toMap(BizHotel::getId, Function.identity()));
        Map<Long, BizScenicSpot> spotMap = spots.stream().collect(Collectors.toMap(BizScenicSpot::getId, Function.identity()));
        Map<Long, BizRestaurant> restaurantMap = restaurants.stream().collect(Collectors.toMap(BizRestaurant::getId, Function.identity()));

        // 2. 全部收藏：构建「用户 -> 收藏列表」与「条目 -> 收藏数」
        List<BizFavorite> allFav = favoriteService.list();
        Map<Long, List<BizFavorite>> favByUser = allFav.stream()
                .collect(Collectors.groupingBy(BizFavorite::getUserId));
        Map<Ref, Long> favCount = allFav.stream()
                .collect(Collectors.groupingBy(f -> ref(f.getTargetType(), f.getTargetId()), Collectors.counting()));

        // 3. 用户行为种子：收藏 + 已支付/已完成订单
        Set<Ref> seeds = new LinkedHashSet<>();
        for (BizFavorite f : favByUser.getOrDefault(userId, List.of())) {
            seeds.add(ref(f.getTargetType(), f.getTargetId()));
        }
        List<BizOrder> myOrders = orderService.lambdaQuery()
                .eq(BizOrder::getUserId, userId)
                .in(BizOrder::getStatus, List.of(1, 3))
                .list();
        for (BizOrder o : myOrders) {
            seeds.add(ref(o.getOrderType(), o.getTargetId()));
        }

        // 4. 三级推荐，按优先级依次填充（保持插入顺序即优先级）
        Map<Ref, Double> weight = new LinkedHashMap<>();
        Map<Ref, String> reason = new HashMap<>();

        itemCF(seeds, favByUser, weight, reason, hotelMap, spotMap, restaurantMap);
        contentBased(seeds, weight, reason, hotels, spots, restaurants, hotelMap, spotMap, restaurantMap);
        hotFallback(seeds, favCount, weight, reason, hotels, spots, restaurants, hotelMap, spotMap, restaurantMap);

        // 5. 截取前 LIMIT 条并组装
        List<RecommendVO.Item> items = new ArrayList<>();
        for (Ref ref : weight.keySet()) {
            if (items.size() >= LIMIT) {
                break;
            }
            RecommendVO.Item it = toItem(ref, reason.get(ref), hotelMap, spotMap, restaurantMap);
            if (it != null) {
                items.add(it);
            }
        }
        log.info("推荐结果 userId={}, 行为数={}, 推荐数={}", userId, seeds.size(), items.size());
        return items;
    }

    // ===================== 第一级：协同过滤 =====================

    private void itemCF(Set<Ref> seeds, Map<Long, List<BizFavorite>> favByUser,
                        Map<Ref, Double> weight, Map<Ref, String> reason,
                        Map<Long, BizHotel> hotelMap, Map<Long, BizScenicSpot> spotMap, Map<Long, BizRestaurant> restaurantMap) {
        if (seeds.isEmpty()) {
            return;
        }
        // 条目 -> 收藏它的用户集合
        Map<Ref, Set<Long>> itemUsers = new HashMap<>();
        for (Map.Entry<Long, List<BizFavorite>> e : favByUser.entrySet()) {
            for (BizFavorite f : e.getValue()) {
                itemUsers.computeIfAbsent(ref(f.getTargetType(), f.getTargetId()), k -> new HashSet<>()).add(e.getKey());
            }
        }

        Map<Ref, Double> cf = new HashMap<>();
        Map<Ref, String> cfReason = new HashMap<>();
        for (Ref seed : seeds) {
            Set<Long> users = itemUsers.get(seed);
            if (users == null || users.isEmpty()) {
                continue;
            }
            String seedName = resolveName(seed, hotelMap, spotMap, restaurantMap);
            for (Long u : users) {
                for (BizFavorite f : favByUser.getOrDefault(u, List.of())) {
                    Ref r = ref(f.getTargetType(), f.getTargetId());
                    if (seeds.contains(r)) {
                        continue; // 用户已喜欢的条目不再推荐
                    }
                    cf.merge(r, 1.0, Double::sum);
                    cfReason.putIfAbsent(r, "喜欢「" + seedName + "」的人也喜欢");
                }
            }
        }
        cf.entrySet().stream()
                .sorted(Map.Entry.<Ref, Double>comparingByValue().reversed())
                .forEach(e -> {
                    weight.putIfAbsent(e.getKey(), e.getValue());
                    reason.putIfAbsent(e.getKey(), cfReason.get(e.getKey()));
                });
    }

    // ===================== 第二级：内容相似 =====================

    private void contentBased(Set<Ref> seeds, Map<Ref, Double> weight, Map<Ref, String> reason,
                              List<BizHotel> hotels, List<BizScenicSpot> spots, List<BizRestaurant> restaurants,
                              Map<Long, BizHotel> hotelMap, Map<Long, BizScenicSpot> spotMap, Map<Long, BizRestaurant> restaurantMap) {
        if (seeds.isEmpty()) {
            return;
        }
        // 汇总种子的分类集合与城市集合
        Set<Long> catIds = new HashSet<>();
        Set<String> cities = new HashSet<>();
        for (Ref seed : seeds) {
            Long catId = categoryIdOf(seed, hotelMap, spotMap, restaurantMap);
            if (catId != null) {
                catIds.add(catId);
            }
            String city = cityOf(seed, hotelMap, spotMap, restaurantMap);
            if (city != null && !city.isBlank()) {
                cities.add(city);
            }
        }

        Map<Ref, Double> cb = new HashMap<>();
        Map<Ref, String> cbReason = new HashMap<>();
        for (BizHotel h : hotels) {
            Ref r = ref(1, h.getId());
            if (seeds.contains(r)) {
                continue;
            }
            addContentCandidate(cb, cbReason, r, h.getRating(), h.getCategoryId(), h.getCity(), catIds, cities);
        }
        for (BizScenicSpot s : spots) {
            Ref r = ref(2, s.getId());
            if (seeds.contains(r)) {
                continue;
            }
            addContentCandidate(cb, cbReason, r, s.getRating(), s.getCategoryId(), s.getCity(), catIds, cities);
        }
        for (BizRestaurant rt : restaurants) {
            Ref r = ref(3, rt.getId());
            if (seeds.contains(r)) {
                continue;
            }
            addContentCandidate(cb, cbReason, r, rt.getRating(), rt.getCategoryId(), rt.getCity(), catIds, cities);
        }
        cb.entrySet().stream()
                .sorted(Map.Entry.<Ref, Double>comparingByValue().reversed())
                .forEach(e -> {
                    weight.putIfAbsent(e.getKey(), e.getValue());
                    reason.putIfAbsent(e.getKey(), cbReason.get(e.getKey()));
                });
    }

    private void addContentCandidate(Map<Ref, Double> cb, Map<Ref, String> cbReason, Ref ref,
                                     BigDecimal rating, Long categoryId, String city,
                                     Set<Long> catIds, Set<String> cities) {
        double score = 0;
        String r = null;
        if (categoryId != null && catIds.contains(categoryId)) {
            score = rating == null ? 3.0 : rating.doubleValue();
            r = "与你喜欢的同类目";
        }
        if (city != null && !city.isBlank() && cities.contains(city)) {
            double cityScore = (rating == null ? 3.0 : rating.doubleValue()) * 0.5;
            if (cityScore > score) {
                score = cityScore;
                r = "同城推荐";
            }
        }
        if (score > 0) {
            cb.put(ref, score);
            cbReason.put(ref, r);
        }
    }

    // ===================== 第三级：热门兜底 =====================

    private void hotFallback(Set<Ref> seeds, Map<Ref, Long> favCount,
                             Map<Ref, Double> weight, Map<Ref, String> reason,
                             List<BizHotel> hotels, List<BizScenicSpot> spots, List<BizRestaurant> restaurants,
                             Map<Long, BizHotel> hotelMap, Map<Long, BizScenicSpot> spotMap, Map<Long, BizRestaurant> restaurantMap) {
        Map<Ref, Double> hot = new HashMap<>();
        for (BizHotel h : hotels) {
            Ref r = ref(1, h.getId());
            if (seeds.contains(r)) {
                continue;
            }
            hot.put(r, hotScore(favCount.getOrDefault(r, 0L), h.getRating()));
        }
        for (BizScenicSpot s : spots) {
            Ref r = ref(2, s.getId());
            if (seeds.contains(r)) {
                continue;
            }
            hot.put(r, hotScore(favCount.getOrDefault(r, 0L), s.getRating()));
        }
        for (BizRestaurant rt : restaurants) {
            Ref r = ref(3, rt.getId());
            if (seeds.contains(r)) {
                continue;
            }
            hot.put(r, hotScore(favCount.getOrDefault(r, 0L), rt.getRating()));
        }
        hot.entrySet().stream()
                .sorted(Map.Entry.<Ref, Double>comparingByValue().reversed())
                .forEach(e -> {
                    weight.putIfAbsent(e.getKey(), e.getValue());
                    reason.putIfAbsent(e.getKey(), "平台热门");
                });
    }

    /** 热门得分：收藏数占主导，评分作微调 */
    private double hotScore(long favCount, BigDecimal rating) {
        double r = rating == null ? 0 : rating.doubleValue();
        return favCount * 10 + r;
    }

    // ===================== 工具方法 =====================

    private RecommendVO.Item toItem(Ref ref, String reasonText,
                                    Map<Long, BizHotel> hotelMap, Map<Long, BizScenicSpot> spotMap, Map<Long, BizRestaurant> restaurantMap) {
        RecommendVO.Item it = new RecommendVO.Item();
        it.setType(ref.type);
        it.setId(ref.id);
        it.setReason(reasonText);
        if (ref.type == 1) {
            BizHotel h = hotelMap.get(ref.id);
            if (h == null) {
                return null;
            }
            it.setName(h.getName());
            it.setCoverImage(h.getCoverImage());
            it.setCity(h.getCity());
            it.setRating(h.getRating());
            it.setSub(h.getCity() + " · " + h.getStarLevel() + "星 · ★" + h.getRating());
        } else if (ref.type == 2) {
            BizScenicSpot s = spotMap.get(ref.id);
            if (s == null) {
                return null;
            }
            it.setName(s.getName());
            it.setCoverImage(s.getCoverImage());
            it.setCity(s.getCity());
            it.setRating(s.getRating());
            it.setSub(s.getCity() + " · ★" + s.getRating());
        } else {
            BizRestaurant r = restaurantMap.get(ref.id);
            if (r == null) {
                return null;
            }
            it.setName(r.getName());
            it.setCoverImage(r.getCoverImage());
            it.setCity(r.getCity());
            it.setRating(r.getRating());
            it.setSub(r.getCity() + " · 人均 ¥" + r.getAvgPrice() + " · ★" + r.getRating());
        }
        return it;
    }

    private String resolveName(Ref ref, Map<Long, BizHotel> hotelMap, Map<Long, BizScenicSpot> spotMap, Map<Long, BizRestaurant> restaurantMap) {
        if (ref.type == 1 && hotelMap.containsKey(ref.id)) {
            return hotelMap.get(ref.id).getName();
        }
        if (ref.type == 2 && spotMap.containsKey(ref.id)) {
            return spotMap.get(ref.id).getName();
        }
        if (ref.type == 3 && restaurantMap.containsKey(ref.id)) {
            return restaurantMap.get(ref.id).getName();
        }
        return "";
    }

    private Long categoryIdOf(Ref ref, Map<Long, BizHotel> hotelMap, Map<Long, BizScenicSpot> spotMap, Map<Long, BizRestaurant> restaurantMap) {
        if (ref.type == 1 && hotelMap.containsKey(ref.id)) {
            return hotelMap.get(ref.id).getCategoryId();
        }
        if (ref.type == 2 && spotMap.containsKey(ref.id)) {
            return spotMap.get(ref.id).getCategoryId();
        }
        if (ref.type == 3 && restaurantMap.containsKey(ref.id)) {
            return restaurantMap.get(ref.id).getCategoryId();
        }
        return null;
    }

    private String cityOf(Ref ref, Map<Long, BizHotel> hotelMap, Map<Long, BizScenicSpot> spotMap, Map<Long, BizRestaurant> restaurantMap) {
        if (ref.type == 1 && hotelMap.containsKey(ref.id)) {
            return hotelMap.get(ref.id).getCity();
        }
        if (ref.type == 2 && spotMap.containsKey(ref.id)) {
            return spotMap.get(ref.id).getCity();
        }
        if (ref.type == 3 && restaurantMap.containsKey(ref.id)) {
            return restaurantMap.get(ref.id).getCity();
        }
        return null;
    }

    private Ref ref(Integer type, Long id) {
        return new Ref(type, id);
    }

    /** 推荐目标引用：类型 + id */
    private static class Ref {
        final int type;
        final long id;

        Ref(Integer type, Long id) {
            this.type = type == null ? 0 : type;
            this.id = id == null ? 0L : id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Ref other)) {
                return false;
            }
            return type == other.type && id == other.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, id);
        }
    }
}
