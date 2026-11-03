package com.travel.controller.open;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.result.R;
import com.travel.entity.BizComment;
import com.travel.service.CommentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公开接口：评价浏览（仅展示审核通过的评价）
 */
@RestController
@RequestMapping("/api/public/comment")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/page")
    public R<Page<BizComment>> page(@RequestParam(defaultValue = "1") long current,
                                    @RequestParam(defaultValue = "10") long size,
                                    @RequestParam Integer targetType,
                                    @RequestParam Long targetId) {
        LambdaQueryWrapper<BizComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizComment::getTargetType, targetType)
                .eq(BizComment::getTargetId, targetId)
                .eq(BizComment::getStatus, 1)
                .orderByDesc(BizComment::getCreateTime);
        return R.ok(commentService.page(new Page<>(current, size), wrapper));
    }
}
