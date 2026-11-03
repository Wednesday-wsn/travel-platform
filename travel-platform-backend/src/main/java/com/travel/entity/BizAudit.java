package com.travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核记录表
 */
@Data
@TableName("biz_audit")
public class BizAudit {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 审核类型：1商家入驻 2酒店 3景点 4餐厅 5评价 6评价撤销申诉 */
    private Integer targetType;

    private Long targetId;

    /** 申请者ID（商家/用户） */
    private Long applicantId;

    /** 审核管理员ID */
    private Long auditorId;

    /** 状态：0待审核 1通过 2驳回 */
    private Integer status;

    private String reason;

    private LocalDateTime createTime;

    private LocalDateTime auditTime;
}
