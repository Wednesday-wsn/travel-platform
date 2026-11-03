package com.travel.controller.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.context.UserContext;
import com.travel.common.exception.BusinessException;
import com.travel.common.result.R;
import com.travel.common.result.ResultCode;
import com.travel.common.utils.CacheEvictor;
import com.travel.entity.BizHotel;
import com.travel.entity.BizHotelRoom;
import com.travel.service.HotelRoomService;
import com.travel.service.HotelService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商家端：酒店与房型管理
 */
@RestController
@RequestMapping("/api/merchant/hotel")
public class MerchantHotelController {

    private final HotelService hotelService;
    private final HotelRoomService roomService;
    private final CacheEvictor cacheEvictor;

    public MerchantHotelController(HotelService hotelService, HotelRoomService roomService, CacheEvictor cacheEvictor) {
        this.hotelService = hotelService;
        this.roomService = roomService;
        this.cacheEvictor = cacheEvictor;
    }

    // ---------- 酒店 ----------

    @GetMapping("/page")
    public R<Page<BizHotel>> page(@RequestParam(defaultValue = "1") long current,
                                  @RequestParam(defaultValue = "10") long size,
                                  @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<BizHotel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizHotel::getMerchantId, UserContext.getUserId());
        if (status != null) {
            wrapper.eq(BizHotel::getStatus, status);
        }
        wrapper.orderByDesc(BizHotel::getCreateTime);
        return R.ok(hotelService.page(new Page<>(current, size), wrapper));
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        BizHotel hotel = checkOwnHotel(id);
        List<BizHotelRoom> rooms = roomService.lambdaQuery()
                .eq(BizHotelRoom::getHotelId, id).list();
        Map<String, Object> data = new HashMap<>();
        data.put("info", hotel);
        data.put("rooms", rooms);
        return R.ok(data);
    }

    @PostMapping
    public R<Void> create(@RequestBody BizHotel hotel) {
        hotel.setId(null);
        hotel.setMerchantId(UserContext.getUserId());
        hotel.setStatus(0); // 待审核
        hotel.setRating(java.math.BigDecimal.valueOf(5.0));
        hotel.setCommentCount(0);
        hotelService.save(hotel);
        return R.ok();
    }

    @PutMapping
    public R<Void> update(@RequestBody BizHotel hotel) {
        checkOwnHotel(hotel.getId());
        hotel.setMerchantId(UserContext.getUserId());
        hotel.setStatus(0); // 修改后重新审核
        hotelService.updateById(hotel);
        cacheEvictor.evictHotel(hotel.getId());
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        checkOwnHotel(id);
        hotelService.removeById(id);
        cacheEvictor.evictHotel(id);
        return R.ok();
    }

    @PutMapping("/{id}/shelf")
    public R<Void> shelf(@PathVariable Long id) {
        BizHotel hotel = checkOwnHotel(id);
        hotel.setStatus(0); // 申请上架：重新进入待审核
        hotelService.updateById(hotel);
        cacheEvictor.evictHotel(id);
        return R.ok();
    }

    @PutMapping("/{id}/off")
    public R<Void> off(@PathVariable Long id) {
        BizHotel hotel = checkOwnHotel(id);
        hotel.setStatus(3); // 商家主动下架
        hotelService.updateById(hotel);
        cacheEvictor.evictHotel(id);
        return R.ok();
    }

    // ---------- 房型 ----------

    @GetMapping("/room/list")
    public R<List<BizHotelRoom>> roomList(@RequestParam Long hotelId) {
        checkOwnHotel(hotelId);
        return R.ok(roomService.lambdaQuery()
                .eq(BizHotelRoom::getHotelId, hotelId).list());
    }

    @PostMapping("/room")
    public R<Void> createRoom(@RequestBody BizHotelRoom room) {
        checkOwnHotel(room.getHotelId());
        room.setId(null);
        room.setStatus(1);
        if (room.getRemainingCount() == null) {
            room.setRemainingCount(room.getTotalCount());
        }
        roomService.save(room);
        cacheEvictor.evictHotel(room.getHotelId());
        return R.ok();
    }

    @PutMapping("/room")
    public R<Void> updateRoom(@RequestBody BizHotelRoom room) {
        BizHotelRoom exist = roomService.getById(room.getId());
        if (exist == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        checkOwnHotel(exist.getHotelId());
        roomService.updateById(room);
        cacheEvictor.evictHotel(exist.getHotelId());
        return R.ok();
    }

    @DeleteMapping("/room/{id}")
    public R<Void> deleteRoom(@PathVariable Long id) {
        BizHotelRoom room = roomService.getById(id);
        if (room != null) {
            checkOwnHotel(room.getHotelId());
            roomService.removeById(id);
            cacheEvictor.evictHotel(room.getHotelId());
        }
        return R.ok();
    }

    private BizHotel checkOwnHotel(Long hotelId) {
        BizHotel hotel = hotelService.getById(hotelId);
        if (hotel == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!hotel.getMerchantId().equals(UserContext.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return hotel;
    }
}
