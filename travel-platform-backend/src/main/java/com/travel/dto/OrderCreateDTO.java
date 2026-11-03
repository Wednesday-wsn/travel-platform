package com.travel.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 下单请求
 */
@Data
public class OrderCreateDTO {

    /** 订单类型：1酒店 2景点 */
    @NotNull(message = "订单类型不能为空")
    private Integer orderType;

    @NotNull(message = "目标ID不能为空")
    private Long targetId;

    private String contactName;

    private String contactPhone;

    private String remark;

    @NotEmpty(message = "订单明细不能为空")
    private List<OrderItemDTO> items;
}
