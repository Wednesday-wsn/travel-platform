package com.travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商家表
 */
@Data
@TableName("biz_merchant")
public class BizMerchant {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String name;

    /** 商家类型：1酒店 2餐厅 3综合 */
    private Integer type;

    private String contactName;

    private String contactPhone;

    private String email;

    private String address;

    private String description;

    private String logo;

    /** 审核状态：0待审核 1通过 2驳回 3禁用 */
    private Integer status;

    private String auditReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
