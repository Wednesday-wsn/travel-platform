package com.travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏表
 */
@Data
@TableName("biz_favorite")
public class BizFavorite {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 目标类型：1酒店 2景点 3餐厅 */
    private Integer targetType;

    private Long targetId;

    private LocalDateTime createTime;
}
