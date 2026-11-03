package com.travel.controller.admin;

import com.travel.common.result.R;
import com.travel.entity.BizOrder;
import com.travel.service.CommentService;
import com.travel.service.HotelService;
import com.travel.service.MerchantService;
import com.travel.service.OrderService;
import com.travel.service.RestaurantService;
import com.travel.service.ScenicSpotService;
import com.travel.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端：平台数据统计
 */
@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    private final UserService userService;
    private final MerchantService merchantService;
    private final HotelService hotelService;
    private final ScenicSpotService scenicSpotService;
    private final RestaurantService restaurantService;
    private final OrderService orderService;
    private final CommentService commentService;

    public AdminStatsController(UserService userService,
                                MerchantService merchantService,
                                HotelService hotelService,
                                ScenicSpotService scenicSpotService,
                                RestaurantService restaurantService,
                                OrderService orderService,
                                CommentService commentService) {
        this.userService = userService;
        this.merchantService = merchantService;
        this.hotelService = hotelService;
        this.scenicSpotService = scenicSpotService;
        this.restaurantService = restaurantService;
        this.orderService = orderService;
        this.commentService = commentService;
    }

    @GetMapping("/overview")
    public R<Map<String, Object>> overview() {
        List<BizOrder> orders = orderService.lambdaQuery()
                .in(BizOrder::getStatus, List.of(1, 3)).list();
        BigDecimal orderAmount = orders.stream()
                .map(BizOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userService.count());
        data.put("merchantCount", merchantService.count());
        data.put("hotelCount", hotelService.count());
        data.put("spotCount", scenicSpotService.count());
        data.put("restaurantCount", restaurantService.count());
        data.put("orderCount", orderService.count());
        data.put("commentCount", commentService.count());
        data.put("orderAmount", orderAmount);
        return R.ok(data);
    }

    @GetMapping("/order-trend")
    public R<List<Map<String, Object>>> orderTrend(@RequestParam(defaultValue = "7") int days) {
        LocalDateTime start = LocalDate.now().minusDays(days).atStartOfDay();
        List<BizOrder> orders = orderService.lambdaQuery()
                .ge(BizOrder::getCreateTime, start).list();

        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            long count = orders.stream()
                    .filter(o -> o.getCreateTime() != null && o.getCreateTime().toLocalDate().equals(day))
                    .count();
            BigDecimal amount = orders.stream()
                    .filter(o -> o.getCreateTime() != null && o.getCreateTime().toLocalDate().equals(day))
                    .filter(o -> o.getStatus() == 1 || o.getStatus() == 3)
                    .map(BizOrder::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> item = new HashMap<>();
            item.put("date", day.toString());
            item.put("count", count);
            item.put("amount", amount);
            result.add(item);
        }
        return R.ok(result);
    }
}
