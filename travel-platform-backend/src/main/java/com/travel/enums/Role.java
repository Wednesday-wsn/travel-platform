package com.travel.enums;

import lombok.Getter;

/**
 * 系统角色
 */
@Getter
public enum Role {

    USER("user", "用户"),
    MERCHANT("merchant", "商家"),
    ADMIN("admin", "管理员");

    private final String code;
    private final String desc;

    Role(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
