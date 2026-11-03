package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 评价回复请求
 */
@Data
public class ReplyDTO {

    @NotBlank(message = "回复内容不能为空")
    private String reply;
}
