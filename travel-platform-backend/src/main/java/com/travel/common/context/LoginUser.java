package com.travel.common.context;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 当前登录用户信息
 */
@Data
@AllArgsConstructor
public class LoginUser {
    /** 用户/商家/管理员 ID */
    private Long id;
    /** 角色：user / merchant / admin */
    private String role;
}
