package com.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.dto.AuditDTO;
import com.travel.entity.BizAudit;

public interface AuditService extends IService<BizAudit> {

    /**
     * 统一审核入口：处理商家入驻、酒店/景点/餐厅信息、评价的审核，
     * 更新目标状态并记录审核日志。
     */
    void audit(AuditDTO dto, Long auditorId);

    /**
     * 商家发起评价撤销申诉：校验评价归属与状态，创建一条待审核的申诉记录。
     *
     * @param commentId  被申诉的评价 ID
     * @param merchantId 发起申诉的商家 ID
     * @param reason     申诉理由
     */
    void appealComment(Long commentId, Long merchantId, String reason);
}
