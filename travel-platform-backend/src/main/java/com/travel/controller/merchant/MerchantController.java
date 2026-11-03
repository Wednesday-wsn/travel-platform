package com.travel.controller.merchant;

import com.travel.common.context.UserContext;
import com.travel.common.exception.BusinessException;
import com.travel.common.result.R;
import com.travel.common.result.ResultCode;
import com.travel.entity.BizMerchant;
import com.travel.service.MerchantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家端：商家档案（我的店铺）
 */
@RestController
@RequestMapping("/api/merchant")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @GetMapping("/profile")
    public R<BizMerchant> profile() {
        BizMerchant merchant = merchantService.getById(UserContext.getUserId());
        if (merchant == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        merchant.setPassword(null); // 脱敏，不返回密码
        return R.ok(merchant);
    }
}
