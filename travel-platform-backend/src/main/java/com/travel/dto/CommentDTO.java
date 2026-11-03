package com.travel.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发表评价请求
 */
@Data
public class CommentDTO {

    /** 目标类型：1酒店 2景点 3餐厅 */
    @NotNull(message = "目标类型不能为空")
    private Integer targetType;

    @NotNull(message = "目标ID不能为空")
    private Long targetId;

    private Long orderId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1分")
    @Max(value = 5, message = "评分最高5分")
    private Integer rating;

    private String content;

    private String images;
}
