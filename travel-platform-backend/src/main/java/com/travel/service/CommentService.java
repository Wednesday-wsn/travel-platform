package com.travel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.dto.CommentDTO;
import com.travel.entity.BizComment;
import com.travel.vo.CommentVO;

public interface CommentService extends IService<BizComment> {

    /**
     * 发表评价（默认待审核）
     */
    void addComment(CommentDTO dto);

    /**
     * 重新计算目标（酒店/景点/餐厅）的评分与评价数
     */
    void recalcRating(Integer targetType, Long targetId);

    /**
     * 管理端分页查询评价（附带目标名称与商家名称）
     */
    Page<CommentVO> pageAdmin(long current, long size, Integer status, Integer targetType);

    /**
     * 商家端分页查询本商家（酒店/餐厅）的已通过评价（附带目标名称与申诉状态）
     */
    Page<CommentVO> pageMerchant(long current, long size, Long merchantId, Integer targetType);
}
