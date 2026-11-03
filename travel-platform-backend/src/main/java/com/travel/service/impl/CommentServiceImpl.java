package com.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.common.context.UserContext;
import com.travel.common.utils.CacheEvictor;
import com.travel.dto.CommentDTO;
import com.travel.entity.BizAudit;
import com.travel.entity.BizComment;
import com.travel.entity.BizHotel;
import com.travel.entity.BizMerchant;
import com.travel.entity.BizRestaurant;
import com.travel.entity.BizScenicSpot;
import com.travel.mapper.BizAuditMapper;
import com.travel.mapper.BizCommentMapper;
import com.travel.service.CommentService;
import com.travel.service.HotelService;
import com.travel.service.MerchantService;
import com.travel.service.RestaurantService;
import com.travel.service.ScenicSpotService;
import com.travel.vo.CommentVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<BizCommentMapper, BizComment> implements CommentService {

    private final HotelService hotelService;
    private final ScenicSpotService scenicSpotService;
    private final RestaurantService restaurantService;
    private final MerchantService merchantService;
    private final BizAuditMapper bizAuditMapper;
    private final CacheEvictor cacheEvictor;

    public CommentServiceImpl(HotelService hotelService,
                              ScenicSpotService scenicSpotService,
                              RestaurantService restaurantService,
                              MerchantService merchantService,
                              BizAuditMapper bizAuditMapper,
                              CacheEvictor cacheEvictor) {
        this.hotelService = hotelService;
        this.scenicSpotService = scenicSpotService;
        this.restaurantService = restaurantService;
        this.merchantService = merchantService;
        this.bizAuditMapper = bizAuditMapper;
        this.cacheEvictor = cacheEvictor;
    }

    @Override
    public void addComment(CommentDTO dto) {
        BizComment comment = new BizComment();
        comment.setUserId(UserContext.getUserId());
        comment.setOrderId(dto.getOrderId());
        comment.setTargetType(dto.getTargetType());
        comment.setTargetId(dto.getTargetId());
        comment.setRating(dto.getRating());
        comment.setContent(dto.getContent());
        comment.setImages(dto.getImages());
        comment.setStatus(0); // 待审核
        save(comment);
    }

    @Override
    public void recalcRating(Integer targetType, Long targetId) {
        List<BizComment> comments = lambdaQuery()
                .eq(BizComment::getTargetType, targetType)
                .eq(BizComment::getTargetId, targetId)
                .eq(BizComment::getStatus, 1)
                .list();

        int count = comments.size();
        BigDecimal avg = count == 0
                ? BigDecimal.valueOf(5.0)
                : comments.stream()
                        .map(c -> BigDecimal.valueOf(c.getRating()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP);

        if (targetType == 1) {
            BizHotel hotel = hotelService.getById(targetId);
            if (hotel != null) {
                hotel.setRating(avg);
                hotel.setCommentCount(count);
                hotelService.updateById(hotel);
                cacheEvictor.evictHotel(targetId);
            }
        } else if (targetType == 2) {
            BizScenicSpot spot = scenicSpotService.getById(targetId);
            if (spot != null) {
                spot.setRating(avg);
                spot.setCommentCount(count);
                scenicSpotService.updateById(spot);
                cacheEvictor.evictSpot(targetId);
            }
        } else if (targetType == 3) {
            BizRestaurant restaurant = restaurantService.getById(targetId);
            if (restaurant != null) {
                restaurant.setRating(avg);
                restaurant.setCommentCount(count);
                restaurantService.updateById(restaurant);
                cacheEvictor.evictRestaurant(targetId);
            }
        }
    }

    @Override
    public Page<CommentVO> pageAdmin(long current, long size, Integer status, Integer targetType) {
        LambdaQueryWrapper<BizComment> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(BizComment::getStatus, status);
        }
        if (targetType != null) {
            wrapper.eq(BizComment::getTargetType, targetType);
        }
        wrapper.orderByDesc(BizComment::getCreateTime);

        Page<BizComment> page = page(new Page<>(current, size), wrapper);
        Page<CommentVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public Page<CommentVO> pageMerchant(long current, long size, Long merchantId, Integer targetType) {
        List<Long> hotelIds = hotelService.lambdaQuery()
                .eq(BizHotel::getMerchantId, merchantId)
                .list().stream().map(BizHotel::getId).toList();
        List<Long> restaurantIds = restaurantService.lambdaQuery()
                .eq(BizRestaurant::getMerchantId, merchantId)
                .list().stream().map(BizRestaurant::getId).toList();

        if (hotelIds.isEmpty() && restaurantIds.isEmpty()) {
            return new Page<>(current, size);
        }

        LambdaQueryWrapper<BizComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizComment::getStatus, 1); // 只看已通过的评价
        if (targetType != null) {
            wrapper.eq(BizComment::getTargetType, targetType);
        }
        if (hotelIds.isEmpty()) {
            wrapper.eq(BizComment::getTargetType, 3).in(BizComment::getTargetId, restaurantIds);
        } else if (restaurantIds.isEmpty()) {
            wrapper.eq(BizComment::getTargetType, 1).in(BizComment::getTargetId, hotelIds);
        } else {
            wrapper.and(w -> w
                    .eq(BizComment::getTargetType, 1).in(BizComment::getTargetId, hotelIds)
                    .or()
                    .eq(BizComment::getTargetType, 3).in(BizComment::getTargetId, restaurantIds));
        }
        wrapper.orderByDesc(BizComment::getCreateTime);

        Page<BizComment> page = page(new Page<>(current, size), wrapper);
        Page<CommentVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(c -> {
            CommentVO vo = toVO(c);
            vo.setAppealStatus(latestAppealStatus(c.getId()));
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    private Integer latestAppealStatus(Long commentId) {
        BizAudit latest = bizAuditMapper.selectOne(new LambdaQueryWrapper<BizAudit>()
                .eq(BizAudit::getTargetType, 6)
                .eq(BizAudit::getTargetId, commentId)
                .orderByDesc(BizAudit::getId)
                .last("LIMIT 1"));
        return latest == null ? null : latest.getStatus();
    }

    private CommentVO toVO(BizComment c) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(c, vo);

        String targetName = null;
        Long merchantId = null;
        if (c.getTargetType() != null && c.getTargetType() == 1) {
            BizHotel h = hotelService.getById(c.getTargetId());
            if (h != null) {
                targetName = h.getName();
                merchantId = h.getMerchantId();
            }
        } else if (c.getTargetType() != null && c.getTargetType() == 2) {
            BizScenicSpot s = scenicSpotService.getById(c.getTargetId());
            if (s != null) {
                targetName = s.getName();
                merchantId = s.getMerchantId();
            }
        } else if (c.getTargetType() != null && c.getTargetType() == 3) {
            BizRestaurant r = restaurantService.getById(c.getTargetId());
            if (r != null) {
                targetName = r.getName();
                merchantId = r.getMerchantId();
            }
        }

        String merchantName = null;
        if (merchantId != null) {
            BizMerchant m = merchantService.getById(merchantId);
            if (m != null) {
                merchantName = m.getName();
            }
        }

        vo.setTargetName(targetName);
        vo.setMerchantName(merchantName);
        return vo;
    }
}
