package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 评价撤销申诉请求
 */
@Data
public class AppealDTO {

    @NotBlank(message = "申诉理由不能为空")
    private String reason;
}
