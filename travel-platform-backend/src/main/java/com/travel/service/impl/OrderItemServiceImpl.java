package com.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.entity.BizOrderItem;
import com.travel.mapper.BizOrderItemMapper;
import com.travel.service.OrderItemService;
import org.springframework.stereotype.Service;

@Service
public class OrderItemServiceImpl extends ServiceImpl<BizOrderItemMapper, BizOrderItem> implements OrderItemService {
}
