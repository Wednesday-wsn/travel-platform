package com.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.common.exception.BusinessException;
import com.travel.common.result.ResultCode;
import com.travel.dto.AuditDTO;
import com.travel.entity.BizAudit;
import com.travel.entity.BizComment;
import com.travel.entity.BizHotel;
import com.travel.entity.BizMerchant;
import com.travel.entity.BizRestaurant;
import com.travel.entity.BizScenicSpot;
import com.travel.mapper.BizAuditMapper;
import com.travel.service.AuditService;
import com.travel.service.CommentService;
import com.travel.service.HotelService;
import com.travel.service.MerchantService;
import com.travel.service.RestaurantService;
import com.travel.service.ScenicSpotService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditServiceImpl extends ServiceImpl<BizAuditMapper, BizAudit> implements AuditService {

    private final MerchantService merchantService;
    private final HotelService hotelService;
    private final ScenicSpotService scenicSpotService;
    private final RestaurantService restaurantService;
    private final CommentService commentService;

    public AuditServiceImpl(MerchantService merchantService,
                            HotelService hotelService,
                            ScenicSpotService scenicSpotService,
                            RestaurantService restaurantService,
                            CommentService commentService) {
        this.merchantService = merchantService;
        this.hotelService = hotelService;
        this.scenicSpotService = scenicSpotService;
        this.restaurantService = restaurantService;
        this.commentService = commentService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(AuditDTO dto, Long auditorId) {
        int targetType = dto.getTargetType();
        int result = dto.getStatus(); // 1通过 2驳回
        Long targetId = dto.getTargetId();

        switch (targetType) {
            case 1 -> { // 商家入驻
                BizMerchant m = merchantService.getById(targetId);
                if (m == null) throw new BusinessException(ResultCode.NOT_FOUND);
                m.setStatus(result == 1 ? 1 : 2);
                if (result == 2) m.setAuditReason(dto.getReason());
                merchantService.updateById(m);
            }
            case 2 -> { // 酒店
                BizHotel h = hotelService.getById(targetId);
                if (h == null) throw new BusinessException(ResultCode.NOT_FOUND);
                h.setStatus(result == 1 ? 1 : 2);
                hotelService.updateById(h);
            }
            case 3 -> { // 景点
                BizScenicSpot s = scenicSpotService.getById(targetId);
                if (s == null) throw new BusinessException(ResultCode.NOT_FOUND);
                s.setStatus(result == 1 ? 1 : 2);
                scenicSpotService.updateById(s);
            }
            case 4 -> { // 餐厅
                BizRestaurant r = restaurantService.getById(targetId);
                if (r == null) throw new BusinessException(ResultCode.NOT_FOUND);
                r.setStatus(result == 1 ? 1 : 2);
                restaurantService.updateById(r);
            }
            case 5 -> { // 评价
                BizComment c = commentService.getById(targetId);
                if (c == null) throw new BusinessException(ResultCode.NOT_FOUND);
                c.setStatus(result == 1 ? 1 : 2);
                commentService.updateById(c);
                commentService.recalcRating(c.getTargetType(), c.getTargetId());
            }
            case 6 -> { // 评价撤销申诉：通过则删除评价
                BizComment c = commentService.getById(targetId);
                if (c == null) throw new BusinessException(ResultCode.NOT_FOUND);
                if (result == 1) {
                    commentService.removeById(targetId);
                    commentService.recalcRating(c.getTargetType(), c.getTargetId());
                }
            }
            default -> throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        // 记录审核日志
        BizAudit audit = new BizAudit();
        audit.setTargetType(targetType);
        audit.setTargetId(targetId);
        audit.setAuditorId(auditorId);
        audit.setStatus(result);
        audit.setReason(dto.getReason());
        audit.setCreateTime(LocalDateTime.now());
        audit.setAuditTime(LocalDateTime.now());
        save(audit);
    }

    @Override
    public void appealComment(Long commentId, Long merchantId, String reason) {
        BizComment comment = commentService.getById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 校验评价属于该商家（仅酒店/餐厅评价归属商家）
        boolean owned = false;
        if (comment.getTargetType() != null && comment.getTargetType() == 1) {
            BizHotel hotel = hotelService.getById(comment.getTargetId());
            owned = hotel != null && hotel.getMerchantId().equals(merchantId);
        } else if (comment.getTargetType() != null && comment.getTargetType() == 3) {
            BizRestaurant restaurant = restaurantService.getById(comment.getTargetId());
            owned = restaurant != null && restaurant.getMerchantId().equals(merchantId);
        }
        if (!owned) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 仅已通过的评价可申诉
        if (comment.getStatus() == null || comment.getStatus() != 1) {
            throw new BusinessException("仅已通过的评价可发起申诉");
        }

        // 已存在待处理申诉则拒绝重复发起
        Long pending = lambdaQuery()
                .eq(BizAudit::getTargetType, 6)
                .eq(BizAudit::getTargetId, commentId)
                .eq(BizAudit::getStatus, 0)
                .count();
        if (pending != null && pending > 0) {
            throw new BusinessException("该评价已有待处理申诉");
        }

        BizAudit appeal = new BizAudit();
        appeal.setTargetType(6);
        appeal.setTargetId(commentId);
        appeal.setApplicantId(merchantId);
        appeal.setStatus(0);
        appeal.setReason(reason);
        appeal.setCreateTime(LocalDateTime.now());
        save(appeal);
    }
}
