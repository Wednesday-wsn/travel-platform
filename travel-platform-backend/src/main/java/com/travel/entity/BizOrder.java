package com.travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单表（统一订单：酒店/景点）
 */
@Data
@TableName("biz_order")
public class BizOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    /** 订单类型：1酒店 2景点 */
    private Integer orderType;

    private Long targetId;

    private String targetName;

    private BigDecimal totalAmount;

    /** 状态：0待支付 1已支付 2已取消 3已完成 4已退款 */
    private Integer status;

    private String contactName;

    private String contactPhone;

    private String remark;

    private LocalDateTime payTime;

    private LocalDateTime cancelTime;

    private LocalDateTime finishTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
