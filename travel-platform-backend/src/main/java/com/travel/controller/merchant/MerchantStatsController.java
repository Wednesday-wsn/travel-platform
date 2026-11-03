package com.travel.controller.merchant;

import com.travel.common.context.UserContext;
import com.travel.common.result.R;
import com.travel.entity.BizHotel;
import com.travel.entity.BizOrder;
import com.travel.entity.BizRestaurant;
import com.travel.service.HotelService;
import com.travel.service.OrderService;
import com.travel.service.RestaurantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商家端：数据统计
 */
@RestController
@RequestMapping("/api/merchant/stats")
public class MerchantStatsController {

    private final HotelService hotelService;
    private final RestaurantService restaurantService;
    private final OrderService orderService;

    public MerchantStatsController(HotelService hotelService,
                                   RestaurantService restaurantService,
                                   OrderService orderService) {
        this.hotelService = hotelService;
        this.restaurantService = restaurantService;
        this.orderService = orderService;
    }

    @GetMapping("/overview")
    public R<Map<String, Object>> overview() {
        Long merchantId = UserContext.getUserId();
        long hotelCount = hotelService.lambdaQuery()
                .eq(BizHotel::getMerchantId, merchantId).count();
        long restaurantCount = restaurantService.lambdaQuery()
                .eq(BizRestaurant::getMerchantId, merchantId).count();

        List<Long> hotelIds = hotelService.lambdaQuery()
                .eq(BizHotel::getMerchantId, merchantId)
                .list().stream().map(BizHotel::getId).toList();

        List<BizOrder> orders;
        if (hotelIds.isEmpty()) {
            orders = List.of();
        } else {
            orders = orderService.lambdaQuery()
                    .eq(BizOrder::getOrderType, 1)
                    .in(BizOrder::getTargetId, hotelIds)
                    .list();
        }

        long orderCount = orders.size();
        BigDecimal orderAmount = orders.stream()
                .filter(o -> o.getStatus() == 1 || o.getStatus() == 3)
                .map(BizOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> data = new HashMap<>();
        data.put("hotelCount", hotelCount);
        data.put("restaurantCount", restaurantCount);
        data.put("orderCount", orderCount);
        data.put("orderAmount", orderAmount);
        return R.ok(data);
    }
}
