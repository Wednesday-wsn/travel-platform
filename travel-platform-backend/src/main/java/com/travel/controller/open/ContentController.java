package com.travel.controller.open;

import com.fasterxml.jackson.core.type.TypeReference;
import com.travel.common.result.R;
import com.travel.common.utils.CacheConstants;
import com.travel.common.utils.RedisUtil;
import com.travel.entity.BizBanner;
import com.travel.entity.BizNotice;
import com.travel.service.BannerService;
import com.travel.service.NoticeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 公开接口：轮播图、公告（接入 Redis 读穿缓存）
 */
@RestController
@RequestMapping("/api/public")
public class ContentController {

    private final BannerService bannerService;
    private final NoticeService noticeService;
    private final RedisUtil redisUtil;

    public ContentController(BannerService bannerService, NoticeService noticeService, RedisUtil redisUtil) {
        this.bannerService = bannerService;
        this.noticeService = noticeService;
        this.redisUtil = redisUtil;
    }

    @GetMapping("/banner/list")
    public R<List<BizBanner>> bannerList() {
        List<BizBanner> cached = redisUtil.get(CacheConstants.BANNER_LIST, new TypeReference<List<BizBanner>>() {
        });
        if (cached != null) {
            return R.ok(cached);
        }
        List<BizBanner> list = bannerService.lambdaQuery()
                .eq(BizBanner::getStatus, 1)
                .orderByAsc(BizBanner::getSort)
                .list();
        redisUtil.set(CacheConstants.BANNER_LIST, list, CacheConstants.DETAIL_TTL, TimeUnit.SECONDS);
        return R.ok(list);
    }

    @GetMapping("/notice/list")
    public R<List<BizNotice>> noticeList() {
        List<BizNotice> cached = redisUtil.get(CacheConstants.NOTICE_LIST, new TypeReference<List<BizNotice>>() {
        });
        if (cached != null) {
            return R.ok(cached);
        }
        List<BizNotice> list = noticeService.lambdaQuery()
                .eq(BizNotice::getStatus, 1)
                .orderByDesc(BizNotice::getCreateTime)
                .list();
        redisUtil.set(CacheConstants.NOTICE_LIST, list, CacheConstants.DETAIL_TTL, TimeUnit.SECONDS);
        return R.ok(list);
    }
}
