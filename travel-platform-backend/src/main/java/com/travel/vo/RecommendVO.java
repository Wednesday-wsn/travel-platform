package com.travel.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 个性化推荐结果：推荐列表，每条带推荐理由。
 */
@Data
public class RecommendVO {

    private List<Item> list;

    @Data
    public static class Item {
        /** 类型：1酒店 2景点 3餐厅 */
        private Integer type;
        private Long id;
        private String name;
        private String coverImage;
        private String city;
        private BigDecimal rating;
        /** 副标题：星级 / 评分 / 人均 */
        private String sub;
        /** 推荐理由，如「喜欢「XX」的用户也喜欢」 */
        private String reason;
    }
}
