package com.travel.controller.admin;

import com.travel.common.context.UserContext;
import com.travel.common.result.R;
import com.travel.common.utils.CacheEvictor;
import com.travel.entity.BizBanner;
import com.travel.entity.BizNotice;
import com.travel.service.BannerService;
import com.travel.service.NoticeService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端：轮播图与公告管理
 */
@RestController
@RequestMapping("/api/admin/content")
public class AdminContentController {

    private final BannerService bannerService;
    private final NoticeService noticeService;
    private final CacheEvictor cacheEvictor;

    public AdminContentController(BannerService bannerService, NoticeService noticeService, CacheEvictor cacheEvictor) {
        this.bannerService = bannerService;
        this.noticeService = noticeService;
        this.cacheEvictor = cacheEvictor;
    }

    // ---------- 轮播图 ----------

    @GetMapping("/banner/list")
    public R<List<BizBanner>> bannerList() {
        return R.ok(bannerService.lambdaQuery()
                .orderByAsc(BizBanner::getSort).list());
    }

    @PostMapping("/banner")
    public R<Void> createBanner(@RequestBody BizBanner banner) {
        banner.setId(null);
        bannerService.save(banner);
        cacheEvictor.evictBanner();
        return R.ok();
    }

    @PutMapping("/banner")
    public R<Void> updateBanner(@RequestBody BizBanner banner) {
        bannerService.updateById(banner);
        cacheEvictor.evictBanner();
        return R.ok();
    }

    @DeleteMapping("/banner/{id}")
    public R<Void> deleteBanner(@PathVariable Long id) {
        bannerService.removeById(id);
        cacheEvictor.evictBanner();
        return R.ok();
    }

    // ---------- 公告 ----------

    @GetMapping("/notice/list")
    public R<List<BizNotice>> noticeList() {
        return R.ok(noticeService.lambdaQuery()
                .orderByDesc(BizNotice::getCreateTime).list());
    }

    @PostMapping("/notice")
    public R<Void> createNotice(@RequestBody BizNotice notice) {
        notice.setId(null);
        notice.setPublisherId(UserContext.getUserId());
        noticeService.save(notice);
        cacheEvictor.evictNotice();
        return R.ok();
    }

    @PutMapping("/notice")
    public R<Void> updateNotice(@RequestBody BizNotice notice) {
        noticeService.updateById(notice);
        cacheEvictor.evictNotice();
        return R.ok();
    }

    @DeleteMapping("/notice/{id}")
    public R<Void> deleteNotice(@PathVariable Long id) {
        noticeService.removeById(id);
        cacheEvictor.evictNotice();
        return R.ok();
    }
}
