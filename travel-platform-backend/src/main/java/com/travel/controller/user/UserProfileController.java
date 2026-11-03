package com.travel.controller.user;

import com.travel.common.context.UserContext;
import com.travel.common.result.R;
import com.travel.entity.SysUser;
import com.travel.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端：个人资料
 */
@RestController
@RequestMapping("/api/user/profile")
public class UserProfileController {

    private final UserService userService;

    public UserProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public R<SysUser> profile() {
        return R.ok(userService.getById(UserContext.getUserId()));
    }

    @PutMapping
    public R<Void> update(@RequestBody SysUser user) {
        user.setId(UserContext.getUserId());
        // 不通过此接口修改用户名与密码
        user.setUsername(null);
        user.setPassword(null);
        user.setDeleted(null);
        userService.updateById(user);
        return R.ok();
    }
}
