package com.travel.controller.merchant;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.context.UserContext;
import com.travel.common.exception.BusinessException;
import com.travel.common.result.R;
import com.travel.common.result.ResultCode;
import com.travel.dto.AppealDTO;
import com.travel.dto.ReplyDTO;
import com.travel.entity.BizComment;
import com.travel.entity.BizHotel;
import com.travel.entity.BizRestaurant;
import com.travel.service.AuditService;
import com.travel.service.CommentService;
import com.travel.service.HotelService;
import com.travel.service.RestaurantService;
import com.travel.vo.CommentVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 商家端：评价管理（本商家酒店/餐厅的评价）
 */
@RestController
@RequestMapping("/api/merchant/comment")
public class MerchantCommentController {

    private final CommentService commentService;
    private final HotelService hotelService;
    private final RestaurantService restaurantService;
    private final AuditService auditService;

    public MerchantCommentController(CommentService commentService,
                                     HotelService hotelService,
                                     RestaurantService restaurantService,
                                     AuditService auditService) {
        this.commentService = commentService;
        this.hotelService = hotelService;
        this.restaurantService = restaurantService;
        this.auditService = auditService;
    }

    @GetMapping("/page")
    public R<Page<CommentVO>> page(@RequestParam(defaultValue = "1") long current,
                                   @RequestParam(defaultValue = "10") long size,
                                   @RequestParam(required = false) Integer targetType) {
        Long merchantId = UserContext.getUserId();
        return R.ok(commentService.pageMerchant(current, size, merchantId, targetType));
    }

    @PutMapping("/{id}/reply")
    public R<Void> reply(@PathVariable Long id, @Valid @RequestBody ReplyDTO dto) {
        BizComment comment = commentService.getById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        checkOwnTarget(comment);
        comment.setReply(dto.getReply());
        comment.setReplyTime(LocalDateTime.now());
        commentService.updateById(comment);
        return R.ok();
    }

    @PostMapping("/{id}/appeal")
    public R<Void> appeal(@PathVariable Long id, @Valid @RequestBody AppealDTO dto) {
        auditService.appealComment(id, UserContext.getUserId(), dto.getReason());
        return R.ok();
    }

    private void checkOwnTarget(BizComment comment) {
        Long merchantId = UserContext.getUserId();
        boolean owned = false;
        if (comment.getTargetType() == 1) {
            BizHotel hotel = hotelService.getById(comment.getTargetId());
            owned = hotel != null && hotel.getMerchantId().equals(merchantId);
        } else if (comment.getTargetType() == 3) {
            BizRestaurant restaurant = restaurantService.getById(comment.getTargetId());
            owned = restaurant != null && restaurant.getMerchantId().equals(merchantId);
        }
        if (!owned) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }
}
