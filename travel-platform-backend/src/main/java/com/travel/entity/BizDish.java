package com.travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品表
 */
@Data
@TableName("biz_dish")
public class BizDish {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long restaurantId;

    private String name;

    /** 菜品分类（招牌/热菜/凉菜等） */
    private String category;

    private BigDecimal price;

    private String image;

    private String description;

    private Integer sales;

    /** 状态：0下架 1上架 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
