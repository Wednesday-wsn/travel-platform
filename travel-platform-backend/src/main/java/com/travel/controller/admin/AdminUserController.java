package com.travel.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.exception.BusinessException;
import com.travel.common.result.R;
import com.travel.common.result.ResultCode;
import com.travel.entity.SysUser;
import com.travel.service.UserService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端：用户管理
 */
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/page")
    public R<Page<SysUser>> page(@RequestParam(defaultValue = "1") long current,
                                 @RequestParam(defaultValue = "10") long size,
                                 @RequestParam(required = false) Integer status,
                                 @RequestParam(required = false) String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        if (StringUtils.hasText(username)) {
            wrapper.like(SysUser::getUsername, username);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        return R.ok(userService.page(new Page<>(current, size), wrapper));
    }

    @PutMapping("/{id}/enable")
    public R<Void> enable(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        user.setStatus(1);
        userService.updateById(user);
        return R.ok();
    }

    @PutMapping("/{id}/disable")
    public R<Void> disable(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        user.setStatus(0);
        userService.updateById(user);
        return R.ok();
    }
}
