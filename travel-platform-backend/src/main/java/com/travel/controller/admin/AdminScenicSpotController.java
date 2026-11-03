package com.travel.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.exception.BusinessException;
import com.travel.common.result.R;
import com.travel.common.result.ResultCode;
import com.travel.common.utils.CacheEvictor;
import com.travel.entity.BizScenicSpot;
import com.travel.entity.BizSpotTicket;
import com.travel.service.ScenicSpotService;
import com.travel.service.SpotTicketService;
import org.springframework.util.StringUtils;
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
 * 管理端：景点与票型管理（平台统一管理，新增直接上架）
 */
@RestController
@RequestMapping("/api/admin/spot")
public class AdminScenicSpotController {

    private final ScenicSpotService spotService;
    private final SpotTicketService ticketService;
    private final CacheEvictor cacheEvictor;

    public AdminScenicSpotController(ScenicSpotService spotService, SpotTicketService ticketService, CacheEvictor cacheEvictor) {
        this.spotService = spotService;
        this.ticketService = ticketService;
        this.cacheEvictor = cacheEvictor;
    }

    // ---------- 景点 ----------

    @GetMapping("/page")
    public R<Page<BizScenicSpot>> page(@RequestParam(defaultValue = "1") long current,
                                       @RequestParam(defaultValue = "10") long size,
                                       @RequestParam(required = false) Integer status,
                                       @RequestParam(required = false) String name) {
        LambdaQueryWrapper<BizScenicSpot> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(BizScenicSpot::getStatus, status);
        }
        if (StringUtils.hasText(name)) {
            wrapper.like(BizScenicSpot::getName, name);
        }
        wrapper.orderByAsc(BizScenicSpot::getId);
        return R.ok(spotService.page(new Page<>(current, size), wrapper));
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        BizScenicSpot spot = spotService.getById(id);
        if (spot == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        List<BizSpotTicket> tickets = ticketService.lambdaQuery()
                .eq(BizSpotTicket::getSpotId, id).list();
        Map<String, Object> data = new HashMap<>();
        data.put("info", spot);
        data.put("tickets", tickets);
        return R.ok(data);
    }

    @PostMapping
    public R<Void> create(@RequestBody BizScenicSpot spot) {
        spot.setId(null);
        spot.setStatus(1); // 直接上架
        spot.setRating(java.math.BigDecimal.valueOf(5.0));
        spot.setCommentCount(0);
        spotService.save(spot);
        cacheEvictor.evictSpot(spot.getId());
        return R.ok();
    }

    @PutMapping
    public R<Void> update(@RequestBody BizScenicSpot spot) {
        spotService.updateById(spot);
        cacheEvictor.evictSpot(spot.getId());
        return R.ok();
    }

    @PutMapping("/{id}/shelf")
    public R<Void> shelf(@PathVariable Long id) {
        BizScenicSpot spot = spotService.getById(id);
        if (spot == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        spot.setStatus(1);
        spotService.updateById(spot);
        cacheEvictor.evictSpot(id);
        return R.ok();
    }

    @PutMapping("/{id}/off")
    public R<Void> off(@PathVariable Long id) {
        BizScenicSpot spot = spotService.getById(id);
        if (spot == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        spot.setStatus(3);
        spotService.updateById(spot);
        cacheEvictor.evictSpot(id);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        spotService.removeById(id);
        cacheEvictor.evictSpot(id);
        return R.ok();
    }

    // ---------- 票型 ----------

    @GetMapping("/ticket/list")
    public R<List<BizSpotTicket>> ticketList(@RequestParam Long spotId) {
        return R.ok(ticketService.lambdaQuery()
                .eq(BizSpotTicket::getSpotId, spotId).list());
    }

    @PostMapping("/ticket")
    public R<Void> createTicket(@RequestBody BizSpotTicket ticket) {
        ticket.setId(null);
        ticket.setStatus(1);
        if (ticket.getSales() == null) {
            ticket.setSales(0);
        }
        ticketService.save(ticket);
        cacheEvictor.evictSpot(ticket.getSpotId());
        return R.ok();
    }

    @PutMapping("/ticket")
    public R<Void> updateTicket(@RequestBody BizSpotTicket ticket) {
        ticketService.updateById(ticket);
        cacheEvictor.evictSpot(ticket.getSpotId());
        return R.ok();
    }

    @DeleteMapping("/ticket/{id}")
    public R<Void> deleteTicket(@PathVariable Long id) {
        BizSpotTicket ticket = ticketService.getById(id);
        if (ticket != null) {
            ticketService.removeById(id);
            cacheEvictor.evictSpot(ticket.getSpotId());
        }
        return R.ok();
    }
}
