package com.travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 景点票型表
 */
@Data
@TableName("biz_spot_ticket")
public class BizSpotTicket {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long spotId;

    private String name;

    private BigDecimal price;

    private Integer stock;

    private Integer sales;

    private Integer validDays;

    private String description;

    /** 状态：0下架 1上架 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
