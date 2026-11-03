package com.travel.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.context.UserContext;
import com.travel.common.exception.BusinessException;
import com.travel.common.result.R;
import com.travel.common.result.ResultCode;
import com.travel.dto.OrderCreateDTO;
import com.travel.entity.BizOrder;
import com.travel.entity.BizOrderItem;
import com.travel.service.OrderItemService;
import com.travel.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户端：订单
 */
@RestController
@RequestMapping("/api/user/order")
public class UserOrderController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;

    public UserOrderController(OrderService orderService, OrderItemService orderItemService) {
        this.orderService = orderService;
        this.orderItemService = orderItemService;
    }

    @PostMapping
    public R<BizOrder> create(@Valid @RequestBody OrderCreateDTO dto) {
        return R.ok(orderService.createOrder(dto));
    }

    @PostMapping("/{id}/pay")
    public R<Void> pay(@PathVariable Long id) {
        orderService.payOrder(id);
        return R.ok();
    }

    @PostMapping("/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return R.ok();
    }

    @PostMapping("/{id}/complete")
    public R<Void> complete(@PathVariable Long id) {
        orderService.completeOrder(id);
        return R.ok();
    }

    @GetMapping("/page")
    public R<Page<BizOrder>> page(@RequestParam(defaultValue = "1") long current,
                                  @RequestParam(defaultValue = "10") long size,
                                  @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<BizOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizOrder::getUserId, UserContext.getUserId());
        if (status != null) {
            wrapper.eq(BizOrder::getStatus, status);
        }
        wrapper.orderByDesc(BizOrder::getCreateTime);
        return R.ok(orderService.page(new Page<>(current, size), wrapper));
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        BizOrder order = orderService.getById(id);
        if (order == null || !order.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        List<BizOrderItem> items = orderItemService.lambdaQuery()
                .eq(BizOrderItem::getOrderId, id).list();
        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("items", items);
        return R.ok(data);
    }
}
