package com.travel.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审核请求
 */
@Data
public class AuditDTO {

    /** 审核类型：1商家入驻 2酒店 3景点 4餐厅 5评价 */
    @NotNull(message = "审核类型不能为空")
    private Integer targetType;

    @NotNull(message = "审核对象ID不能为空")
    private Long targetId;

    /** 结果：1通过 2驳回 */
    @NotNull(message = "审核结果不能为空")
    private Integer status;

    private String reason;
}
