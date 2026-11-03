package com.travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单明细表
 */
@Data
@TableName("biz_order_item")
public class BizOrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    /** 明细类型：1房型 2票型 */
    private Integer itemType;

    private Long itemId;

    private String itemName;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal subtotal;

    /** 入住日期（酒店） */
    private LocalDate checkInDate;

    /** 离店日期（酒店） */
    private LocalDate checkOutDate;

    /** 游玩日期（景点） */
    private LocalDate visitDate;

    private LocalDateTime createTime;
}
