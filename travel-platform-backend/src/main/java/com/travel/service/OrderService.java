package com.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.dto.OrderCreateDTO;
import com.travel.entity.BizOrder;

public interface OrderService extends IService<BizOrder> {

    /**
     * 下单（含库存校验与扣减）
     */
    BizOrder createOrder(OrderCreateDTO dto);

    /**
     * 模拟支付
     */
    void payOrder(Long orderId);

    /**
     * 取消订单（回补库存）
     */
    void cancelOrder(Long orderId);

    /**
     * 确认完成（已支付 -> 已完成）
     */
    void completeOrder(Long orderId);
}
