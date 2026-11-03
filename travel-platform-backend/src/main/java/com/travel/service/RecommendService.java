package com.travel.service;

import com.travel.vo.RecommendVO;

import java.util.List;

/**
 * 个性化推荐服务。
 * <p>
 * 采用「三级混合推荐策略」：
 * 1. 基于物品的协同过滤（Item-CF，利用收藏共现）；
 * 2. 内容相似兜底（同分类 / 同城市）；
 * 3. 热门兜底（收藏数 + 评分，应对冷启动）。
 */
public interface RecommendService {

    /**
     * 为用户生成个性化推荐列表
     *
     * @param userId 用户 id
     * @return 推荐条目（最多 10 条）
     */
    List<RecommendVO.Item> recommend(Long userId);
}
