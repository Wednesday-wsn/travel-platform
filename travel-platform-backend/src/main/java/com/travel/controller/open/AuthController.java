package com.travel.controller.open;

import com.travel.common.result.R;
import com.travel.dto.LoginDTO;
import com.travel.dto.MerchantRegisterDTO;
import com.travel.dto.UserRegisterDTO;
import com.travel.service.AdminService;
import com.travel.service.MerchantService;
import com.travel.service.UserService;
import com.travel.vo.LoginVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公开接口：三端登录/注册
 */
@RestController
@RequestMapping("/api/public")
public class AuthController {

    private final UserService userService;
    private final MerchantService merchantService;
    private final AdminService adminService;

    public AuthController(UserService userService, MerchantService merchantService, AdminService adminService) {
        this.userService = userService;
        this.merchantService = merchantService;
        this.adminService = adminService;
    }

    @PostMapping("/user/login")
    public R<LoginVO> userLogin(@Valid @RequestBody LoginDTO dto) {
        return R.ok(userService.login(dto));
    }

    @PostMapping("/user/register")
    public R<Void> userRegister(@Valid @RequestBody UserRegisterDTO dto) {
        userService.register(dto);
        return R.ok();
    }

    @PostMapping("/merchant/login")
    public R<LoginVO> merchantLogin(@Valid @RequestBody LoginDTO dto) {
        return R.ok(merchantService.login(dto));
    }

    @PostMapping("/merchant/register")
    public R<Void> merchantRegister(@Valid @RequestBody MerchantRegisterDTO dto) {
        merchantService.register(dto);
        return R.ok();
    }

    @PostMapping("/admin/login")
    public R<LoginVO> adminLogin(@Valid @RequestBody LoginDTO dto) {
        return R.ok(adminService.login(dto));
    }
}
