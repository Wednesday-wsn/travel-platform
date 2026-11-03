package com.travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 轮播图表
 */
@Data
@TableName("biz_banner")
public class BizBanner {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String image;

    /** 跳转类型：0无 1酒店 2景点 3餐厅 */
    private Integer linkType;

    private Long linkId;

    private Integer sort;

    /** 状态：0下架 1上架 */
    private Integer status;

    private LocalDateTime createTime;
}
