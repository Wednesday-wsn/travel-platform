package com.travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 酒店房型表
 */
@Data
@TableName("biz_hotel_room")
public class BizHotelRoom {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long hotelId;

    private String name;

    /** 单价（元/晚） */
    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer totalCount;

    private Integer remainingCount;

    private String bedType;

    private String area;

    private Integer maxGuests;

    /** 是否含早：0否 1是 */
    private Integer breakfast;

    private String image;

    private String description;

    /** 状态：0下架 1上架 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
