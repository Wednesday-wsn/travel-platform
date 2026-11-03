package com.travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价表
 */
@Data
@TableName("biz_comment")
public class BizComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 关联订单ID（可空） */
    private Long orderId;

    /** 目标类型：1酒店 2景点 3餐厅 */
    private Integer targetType;

    private Long targetId;

    /** 评分：1~5 */
    private Integer rating;

    private String content;

    /** 图片集（逗号分隔） */
    private String images;

    /** 状态：0待审核 1通过 2驳回 */
    private Integer status;

    /** 商家回复 */
    private String reply;

    private LocalDateTime replyTime;

    private LocalDateTime createTime;
}
