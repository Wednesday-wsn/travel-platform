package com.travel.controller.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.context.UserContext;
import com.travel.common.exception.BusinessException;
import com.travel.common.result.R;
import com.travel.common.result.ResultCode;
import com.travel.entity.BizHotel;
import com.travel.entity.BizOrder;
import com.travel.service.HotelService;
import com.travel.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商家端：订单管理（本商家酒店订单）
 */
@RestController
@RequestMapping("/api/merchant/order")
public class MerchantOrderController {

    private final OrderService orderService;
    private final HotelService hotelService;

    public MerchantOrderController(OrderService orderService, HotelService hotelService) {
        this.orderService = orderService;
        this.hotelService = hotelService;
    }

    @GetMapping("/page")
    public R<Page<BizOrder>> page(@RequestParam(defaultValue = "1") long current,
                                  @RequestParam(defaultValue = "10") long size,
                                  @RequestParam(required = false) Integer status) {
        List<Long> hotelIds = hotelService.lambdaQuery()
                .eq(BizHotel::getMerchantId, UserContext.getUserId())
                .list()
                .stream()
                .map(BizHotel::getId)
                .toList();
        if (hotelIds.isEmpty()) {
            return R.ok(new Page<>(current, size));
        }
        LambdaQueryWrapper<BizOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizOrder::getOrderType, 1)
                .in(BizOrder::getTargetId, hotelIds);
        if (status != null) {
            wrapper.eq(BizOrder::getStatus, status);
        }
        wrapper.orderByDesc(BizOrder::getCreateTime);
        return R.ok(orderService.page(new Page<>(current, size), wrapper));
    }

    @PutMapping("/{id}/finish")
    public R<Void> finish(@PathVariable Long id) {
        BizOrder order = orderService.getById(id);
        if (order == null || order.getOrderType() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        BizHotel hotel = hotelService.getById(order.getTargetId());
        if (hotel == null || !hotel.getMerchantId().equals(UserContext.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (order.getStatus() != 1) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        order.setStatus(3); // 已完成
        order.setFinishTime(LocalDateTime.now());
        orderService.updateById(order);
        return R.ok();
    }
}
