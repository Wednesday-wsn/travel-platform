package com.travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类表
 */
@Data
@TableName("biz_category")
public class BizCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类类型：1酒店 2景点 3餐厅 4菜品 */
    private Integer type;

    private String name;

    private Integer sort;

    private LocalDateTime createTime;
}
