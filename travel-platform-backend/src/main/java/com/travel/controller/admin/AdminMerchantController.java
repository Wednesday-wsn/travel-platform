package com.travel.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.exception.BusinessException;
import com.travel.common.result.R;
import com.travel.common.result.ResultCode;
import com.travel.entity.BizMerchant;
import com.travel.service.MerchantService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端：商家管理
 */
@RestController
@RequestMapping("/api/admin/merchant")
public class AdminMerchantController {

    private final MerchantService merchantService;

    public AdminMerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @GetMapping("/page")
    public R<Page<BizMerchant>> page(@RequestParam(defaultValue = "1") long current,
                                     @RequestParam(defaultValue = "10") long size,
                                     @RequestParam(required = false) Integer status,
                                     @RequestParam(required = false) String name) {
        LambdaQueryWrapper<BizMerchant> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(BizMerchant::getStatus, status);
        }
        if (StringUtils.hasText(name)) {
            wrapper.like(BizMerchant::getName, name);
        }
        wrapper.orderByDesc(BizMerchant::getCreateTime);
        return R.ok(merchantService.page(new Page<>(current, size), wrapper));
    }

    @PutMapping("/{id}/enable")
    public R<Void> enable(@PathVariable Long id) {
        BizMerchant merchant = merchantService.getById(id);
        if (merchant == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        merchant.setStatus(1);
        merchantService.updateById(merchant);
        return R.ok();
    }

    @PutMapping("/{id}/disable")
    public R<Void> disable(@PathVariable Long id) {
        BizMerchant merchant = merchantService.getById(id);
        if (merchant == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        merchant.setStatus(3);
        merchantService.updateById(merchant);
        return R.ok();
    }
}
