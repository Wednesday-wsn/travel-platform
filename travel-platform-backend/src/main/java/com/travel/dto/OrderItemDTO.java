package com.travel.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 订单明细请求
 */
@Data
public class OrderItemDTO {

    /** 明细类型：1房型 2票型 */
    @NotNull(message = "明细类型不能为空")
    private Integer itemType;

    @NotNull(message = "明细ID不能为空")
    private Long itemId;

    @NotNull(message = "数量不能为空")
    private Integer quantity;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private LocalDate visitDate;
}
