package com.travel.controller.open;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.travel.common.exception.BusinessException;
import com.travel.common.result.R;
import com.travel.common.result.ResultCode;
import com.travel.common.utils.CacheConstants;
import com.travel.common.utils.RedisUtil;
import com.travel.entity.BizHotel;
import com.travel.entity.BizHotelRoom;
import com.travel.service.HotelRoomService;
import com.travel.service.HotelService;
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
 * 公开接口：酒店浏览（列表与详情接入 Redis 读穿缓存）
 */
@RestController
@RequestMapping("/api/public/hotel")
public class HotelController {

    private final HotelService hotelService;
    private final HotelRoomService roomService;
    private final RedisUtil redisUtil;

    public HotelController(HotelService hotelService, HotelRoomService roomService, RedisUtil redisUtil) {
        this.hotelService = hotelService;
        this.roomService = roomService;
        this.redisUtil = redisUtil;
    }

    @GetMapping("/page")
    public R<Page<BizHotel>> page(@RequestParam(defaultValue = "1") long current,
                                  @RequestParam(defaultValue = "10") long size,
                                  @RequestParam(required = false) String name,
                                  @RequestParam(required = false) String city,
                                  @RequestParam(required = false) Long categoryId) {
        String key = CacheConstants.HOTEL_LIST + current + ":" + size + ":"
                + (name == null ? "" : name) + ":"
                + (city == null ? "" : city) + ":"
                + (categoryId == null ? "" : categoryId);
        Page<BizHotel> cached = redisUtil.get(key, new TypeReference<Page<BizHotel>>() {
        });
        if (cached != null) {
            return R.ok(cached);
        }

        LambdaQueryWrapper<BizHotel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizHotel::getStatus, 1);
        if (StringUtils.hasText(name)) {
            wrapper.like(BizHotel::getName, name);
        }
        if (StringUtils.hasText(city)) {
            wrapper.eq(BizHotel::getCity, city);
        }
        if (categoryId != null) {
            wrapper.eq(BizHotel::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(BizHotel::getCreateTime);
        Page<BizHotel> result = hotelService.page(new Page<>(current, size), wrapper);
        // 空结果同样缓存（防缓存穿透），由业务侧查询过滤保证只缓存 status=1 的数据
        redisUtil.set(key, result, CacheConstants.LIST_TTL, TimeUnit.SECONDS);
        return R.ok(result);
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        BizHotel hotel = redisUtil.get(CacheConstants.HOTEL_DETAIL + id, BizHotel.class);
        if (hotel == null) {
            hotel = hotelService.getById(id);
            if (hotel != null && hotel.getStatus() == 1) {
                redisUtil.set(CacheConstants.HOTEL_DETAIL + id, hotel, CacheConstants.DETAIL_TTL, TimeUnit.SECONDS);
            }
        }
        if (hotel == null || hotel.getStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        List<BizHotelRoom> rooms = redisUtil.get(CacheConstants.HOTEL_ROOMS + id, new TypeReference<List<BizHotelRoom>>() {
        });
        if (rooms == null) {
            rooms = roomService.lambdaQuery()
                    .eq(BizHotelRoom::getHotelId, id)
                    .eq(BizHotelRoom::getStatus, 1)
                    .list();
            redisUtil.set(CacheConstants.HOTEL_ROOMS + id, rooms, CacheConstants.DETAIL_TTL, TimeUnit.SECONDS);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("info", hotel);
        data.put("rooms", rooms);
        return R.ok(data);
    }
}
