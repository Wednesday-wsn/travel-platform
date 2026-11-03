package com.travel.controller.open;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.travel.common.exception.BusinessException;
import com.travel.common.result.R;
import com.travel.common.result.ResultCode;
import com.travel.common.utils.CacheConstants;
import com.travel.common.utils.RedisUtil;
import com.travel.entity.BizScenicSpot;
import com.travel.entity.BizSpotTicket;
import com.travel.service.ScenicSpotService;
import com.travel.service.SpotTicketService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 公开接口：景点浏览（列表与详情接入 Redis 读穿缓存）
 */
@RestController
@RequestMapping("/api/public/spot")
public class ScenicSpotController {

    private final ScenicSpotService spotService;
    private final SpotTicketService ticketService;
    private final RedisUtil redisUtil;

    public ScenicSpotController(ScenicSpotService spotService, SpotTicketService ticketService, RedisUtil redisUtil) {
        this.spotService = spotService;
        this.ticketService = ticketService;
        this.redisUtil = redisUtil;
    }

    @GetMapping("/page")
    public R<Page<BizScenicSpot>> page(@RequestParam(defaultValue = "1") long current,
                                       @RequestParam(defaultValue = "10") long size,
                                       @RequestParam(required = false) String name,
                                       @RequestParam(required = false) String city,
                                       @RequestParam(required = false) Long categoryId) {
        String key = CacheConstants.SPOT_LIST + current + ":" + size + ":"
                + (name == null ? "" : name) + ":"
                + (city == null ? "" : city) + ":"
                + (categoryId == null ? "" : categoryId);
        Page<BizScenicSpot> cached = redisUtil.get(key, new TypeReference<Page<BizScenicSpot>>() {
        });
        if (cached != null) {
            return R.ok(cached);
        }

        LambdaQueryWrapper<BizScenicSpot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizScenicSpot::getStatus, 1);
        if (StringUtils.hasText(name)) {
            wrapper.like(BizScenicSpot::getName, name);
        }
        if (StringUtils.hasText(city)) {
            wrapper.eq(BizScenicSpot::getCity, city);
        }
        if (categoryId != null) {
            wrapper.eq(BizScenicSpot::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(BizScenicSpot::getCreateTime);
        Page<BizScenicSpot> result = spotService.page(new Page<>(current, size), wrapper);
        redisUtil.set(key, result, CacheConstants.LIST_TTL, TimeUnit.SECONDS);
        return R.ok(result);
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        BizScenicSpot spot = redisUtil.get(CacheConstants.SPOT_DETAIL + id, BizScenicSpot.class);
        if (spot == null) {
            spot = spotService.getById(id);
            if (spot != null && spot.getStatus() == 1) {
                redisUtil.set(CacheConstants.SPOT_DETAIL + id, spot, CacheConstants.DETAIL_TTL, TimeUnit.SECONDS);
            }
        }
        if (spot == null || spot.getStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        List<BizSpotTicket> tickets = redisUtil.get(CacheConstants.SPOT_TICKETS + id, new TypeReference<List<BizSpotTicket>>() {
        });
        if (tickets == null) {
            tickets = ticketService.lambdaQuery()
                    .eq(BizSpotTicket::getSpotId, id)
                    .eq(BizSpotTicket::getStatus, 1)
                    .list();
            redisUtil.set(CacheConstants.SPOT_TICKETS + id, tickets, CacheConstants.DETAIL_TTL, TimeUnit.SECONDS);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("info", spot);
        data.put("tickets", tickets);
        return R.ok(data);
    }
}
