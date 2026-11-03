package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商家注册（入驻申请）请求
 */
@Data
public class MerchantRegisterDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String name;

    /** 商家类型：1酒店 2餐厅 3综合 */
    @NotNull(message = "商家类型不能为空")
    private Integer type;

    private String contactName;

    private String contactPhone;

    private String email;

    private String address;

    private String description;

    private String logo;
}
