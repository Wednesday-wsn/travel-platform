package com.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.common.context.UserContext;
import com.travel.common.exception.BusinessException;
import com.travel.common.result.ResultCode;
import com.travel.common.utils.CacheEvictor;
import com.travel.dto.OrderCreateDTO;
import com.travel.dto.OrderItemDTO;
import com.travel.entity.BizHotel;
import com.travel.entity.BizHotelRoom;
import com.travel.entity.BizOrder;
import com.travel.entity.BizOrderItem;
import com.travel.entity.BizScenicSpot;
import com.travel.entity.BizSpotTicket;
import com.travel.mapper.BizHotelRoomMapper;
import com.travel.mapper.BizOrderMapper;
import com.travel.mapper.BizSpotTicketMapper;
import com.travel.service.HotelRoomService;
import com.travel.service.HotelService;
import com.travel.service.OrderItemService;
import com.travel.service.OrderService;
import com.travel.service.ScenicSpotService;
import com.travel.service.SpotTicketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl extends ServiceImpl<BizOrderMapper, BizOrder> implements OrderService {

    private final HotelService hotelService;
    private final ScenicSpotService scenicSpotService;
    private final HotelRoomService hotelRoomService;
    private final SpotTicketService spotTicketService;
    private final OrderItemService orderItemService;
    private final BizHotelRoomMapper hotelRoomMapper;
    private final BizSpotTicketMapper spotTicketMapper;
    private final CacheEvictor cacheEvictor;

    public OrderServiceImpl(HotelService hotelService,
                            ScenicSpotService scenicSpotService,
                            HotelRoomService hotelRoomService,
                            SpotTicketService spotTicketService,
                            OrderItemService orderItemService,
                            BizHotelRoomMapper hotelRoomMapper,
                            BizSpotTicketMapper spotTicketMapper,
                            CacheEvictor cacheEvictor) {
        this.hotelService = hotelService;
        this.scenicSpotService = scenicSpotService;
        this.hotelRoomService = hotelRoomService;
        this.spotTicketService = spotTicketService;
        this.orderItemService = orderItemService;
        this.hotelRoomMapper = hotelRoomMapper;
        this.spotTicketMapper = spotTicketMapper;
        this.cacheEvictor = cacheEvictor;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizOrder createOrder(OrderCreateDTO dto) {
        Long userId = UserContext.getUserId();
        Integer orderType = dto.getOrderType();

        // 1. 校验目标并确定名称快照
        String targetName;
        if (orderType == 1) {
            BizHotel hotel = hotelService.getById(dto.getTargetId());
            if (hotel == null || hotel.getStatus() != 1) {
                throw new BusinessException("酒店不存在或未上架");
            }
            targetName = hotel.getName();
        } else if (orderType == 2) {
            BizScenicSpot spot = scenicSpotService.getById(dto.getTargetId());
            if (spot == null || spot.getStatus() != 1) {
                throw new BusinessException("景点不存在或未上架");
            }
            targetName = spot.getName();
        } else {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        // 2. 处理明细：校验并扣减库存，计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<BizOrderItem> orderItems = new ArrayList<>();
        for (OrderItemDTO itemDTO : dto.getItems()) {
            if (itemDTO.getQuantity() == null || itemDTO.getQuantity() <= 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }
            BizOrderItem item = new BizOrderItem();
            BigDecimal subtotal;
            if (itemDTO.getItemType() == 1) {
                // 酒店房型
                BizHotelRoom room = hotelRoomService.getById(itemDTO.getItemId());
                if (room == null || room.getStatus() != 1) {
                    throw new BusinessException("房型不存在或已下架");
                }
                if (itemDTO.getCheckInDate() == null || itemDTO.getCheckOutDate() == null) {
                    throw new BusinessException("请选择入住和离店日期");
                }
                long nights = ChronoUnit.DAYS.between(itemDTO.getCheckInDate(), itemDTO.getCheckOutDate());
                if (nights <= 0) {
                    throw new BusinessException("离店日期必须晚于入住日期");
                }
                int rows = hotelRoomMapper.deductStock(room.getId(), itemDTO.getQuantity());
                if (rows == 0) {
                    throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
                }
                subtotal = room.getPrice()
                        .multiply(BigDecimal.valueOf(nights))
                        .multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
                item.setItemName(room.getName());
                item.setPrice(room.getPrice());
                item.setCheckInDate(itemDTO.getCheckInDate());
                item.setCheckOutDate(itemDTO.getCheckOutDate());
            } else if (itemDTO.getItemType() == 2) {
                // 景点票型
                BizSpotTicket ticket = spotTicketService.getById(itemDTO.getItemId());
                if (ticket == null || ticket.getStatus() != 1) {
                    throw new BusinessException("票型不存在或已下架");
                }
                int rows = spotTicketMapper.deductStock(ticket.getId(), itemDTO.getQuantity());
                if (rows == 0) {
                    throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
                }
                subtotal = ticket.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
                item.setItemName(ticket.getName());
                item.setPrice(ticket.getPrice());
                item.setVisitDate(itemDTO.getVisitDate());
            } else {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }
            item.setItemType(itemDTO.getItemType());
            item.setItemId(itemDTO.getItemId());
            item.setQuantity(itemDTO.getQuantity());
            item.setSubtotal(subtotal);
            orderItems.add(item);
            totalAmount = totalAmount.add(subtotal);
        }

        // 3. 保存订单
        BizOrder order = new BizOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setOrderType(orderType);
        order.setTargetId(dto.getTargetId());
        order.setTargetName(targetName);
        order.setTotalAmount(totalAmount);
        order.setStatus(0);
        order.setContactName(dto.getContactName());
        order.setContactPhone(dto.getContactPhone());
        order.setRemark(dto.getRemark());
        save(order);

        // 4. 保存明细
        for (BizOrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemService.save(item);
        }

        // 5. 用户行为变化，失效推荐结果缓存
        cacheEvictor.evictRecommend(userId);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId) {
        BizOrder order = getById(orderId);
        checkOwnOrder(order);
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId) {
        BizOrder order = getById(orderId);
        checkOwnOrder(order);
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        // 回补库存
        List<BizOrderItem> items = orderItemService.lambdaQuery()
                .eq(BizOrderItem::getOrderId, orderId).list();
        for (BizOrderItem item : items) {
            if (item.getItemType() == 1) {
                hotelRoomMapper.restoreStock(item.getItemId(), item.getQuantity());
            } else if (item.getItemType() == 2) {
                spotTicketMapper.restoreStock(item.getItemId(), item.getQuantity());
            }
        }
        order.setStatus(2);
        order.setCancelTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long orderId) {
        BizOrder order = getById(orderId);
        checkOwnOrder(order);
        if (order.getStatus() != 1) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        order.setStatus(3);
        order.setFinishTime(LocalDateTime.now());
        updateById(order);
    }

    private void checkOwnOrder(BizOrder order) {
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!order.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    private String generateOrderNo() {
        return "T" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000));
    }
}
