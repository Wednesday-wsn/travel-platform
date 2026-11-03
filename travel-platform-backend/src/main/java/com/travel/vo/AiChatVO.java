package com.travel.vo;

import lombok.Data;

import java.util.List;

/**
 * AI 问答返回结果：自然语言回复 + 可点击的推荐卡片
 */
@Data
public class AiChatVO {

    /** 自然语言回复 */
    private String reply;

    /** 推荐实体列表（可空），前端渲染成卡片 */
    private List<Item> items;

    @Data
    public static class Item {
        private Long id;
        /** 类型：1酒店 2景点 3餐厅 */
        private Integer type;
        private String name;
        /** 副标题：星级 / 评分 / 价格等 */
        private String sub;
    }
}
