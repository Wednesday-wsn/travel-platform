package com.travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 景点表
 */
@Data
@TableName("biz_scenic_spot")
public class BizScenicSpot {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属商家ID（可空，平台统一管理） */
    private Long merchantId;

    private String name;

    private Long categoryId;

    private String province;

    private String city;

    private String address;

    private String openTime;

    private String coverImage;

    /** 图片集（逗号分隔） */
    private String images;

    private String description;

    private BigDecimal rating;

    private Integer commentCount;

    /** 状态：0待审核 1上架 2驳回 3下架 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
