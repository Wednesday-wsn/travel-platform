package com.travel.vo;

import com.travel.entity.BizComment;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评价 VO：在评价字段基础上附带目标名称与所属商家名称
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentVO extends BizComment {

    /** 目标名称（酒店/景点/餐厅名称） */
    private String targetName;

    /** 所属商家名称（景区可能为空） */
    private String merchantName;

    /** 该评价的撤销申诉状态：null无申诉 0待处理 1已撤销 2已驳回 */
    private Integer appealStatus;
}
