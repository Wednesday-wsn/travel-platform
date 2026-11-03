package com.travel.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录返回结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    private String token;

    private Long id;

    /** 角色：user / merchant / admin */
    private String role;

    /** 昵称 / 商家名 / 姓名 */
    private String name;

    private String username;

    /** 头像 / Logo */
    private String avatar;

    /** token 过期时间戳（毫秒） */
    private Long expireAt;
}
