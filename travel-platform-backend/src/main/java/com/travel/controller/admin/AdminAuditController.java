package com.travel.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.context.UserContext;
import com.travel.common.result.R;
import com.travel.dto.AuditDTO;
import com.travel.entity.BizAudit;
import com.travel.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端：统一审核
 */
@RestController
@RequestMapping("/api/admin/audit")
public class AdminAuditController {

    private final AuditService auditService;

    public AdminAuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * 统一审核：targetType 1商家入驻 2酒店 3景点 4餐厅 5评价
     */
    @PostMapping
    public R<Void> audit(@Valid @RequestBody AuditDTO dto) {
        auditService.audit(dto, UserContext.getUserId());
        return R.ok();
    }

    @GetMapping("/page")
    public R<Page<BizAudit>> page(@RequestParam(defaultValue = "1") long current,
                                  @RequestParam(defaultValue = "10") long size,
                                  @RequestParam(required = false) Integer targetType,
                                  @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<BizAudit> wrapper = new LambdaQueryWrapper<>();
        if (targetType != null) {
            wrapper.eq(BizAudit::getTargetType, targetType);
        }
        if (status != null) {
            wrapper.eq(BizAudit::getStatus, status);
        }
        wrapper.orderByDesc(BizAudit::getCreateTime);
        return R.ok(auditService.page(new Page<>(current, size), wrapper));
    }
}
