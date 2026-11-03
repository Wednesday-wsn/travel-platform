package com.travel.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.exception.BusinessException;
import com.travel.common.result.R;
import com.travel.common.result.ResultCode;
import com.travel.common.utils.CacheEvictor;
import com.travel.entity.BizHotel;
import com.travel.service.HotelService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端：酒店监管（上架/下架/删除；审核走统一审核接口）
 */
@RestController
@RequestMapping("/api/admin/hotel")
public class AdminHotelController {

    private final HotelService hotelService;
    private final CacheEvictor cacheEvictor;

    public AdminHotelController(HotelService hotelService, CacheEvictor cacheEvictor) {
        this.hotelService = hotelService;
        this.cacheEvictor = cacheEvictor;
    }

    @GetMapping("/page")
    public R<Page<BizHotel>> page(@RequestParam(defaultValue = "1") long current,
                                  @RequestParam(defaultValue = "10") long size,
                                  @RequestParam(required = false) Integer status,
                                  @RequestParam(required = false) String name) {
        LambdaQueryWrapper<BizHotel> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(BizHotel::getStatus, status);
        }
        if (StringUtils.hasText(name)) {
            wrapper.like(BizHotel::getName, name);
        }
        wrapper.orderByAsc(BizHotel::getId);
        return R.ok(hotelService.page(new Page<>(current, size), wrapper));
    }

    @PutMapping("/{id}/shelf")
    public R<Void> shelf(@PathVariable Long id) {
        BizHotel hotel = hotelService.getById(id);
        if (hotel == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        hotel.setStatus(1);
        hotelService.updateById(hotel);
        cacheEvictor.evictHotel(id);
        return R.ok();
    }

    @PutMapping("/{id}/off")
    public R<Void> off(@PathVariable Long id) {
        BizHotel hotel = hotelService.getById(id);
        if (hotel == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        hotel.setStatus(3);
        hotelService.updateById(hotel);
        cacheEvictor.evictHotel(id);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        hotelService.removeById(id);
        cacheEvictor.evictHotel(id);
        return R.ok();
    }
}
