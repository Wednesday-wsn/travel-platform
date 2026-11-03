package com.travel.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.context.UserContext;
import com.travel.common.result.R;
import com.travel.dto.CommentDTO;
import com.travel.entity.BizComment;
import com.travel.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端：评价
 */
@RestController
@RequestMapping("/api/user/comment")
public class UserCommentController {

    private final CommentService commentService;

    public UserCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public R<Void> add(@Valid @RequestBody CommentDTO dto) {
        commentService.addComment(dto);
        return R.ok();
    }

    @GetMapping("/page")
    public R<Page<BizComment>> page(@RequestParam(defaultValue = "1") long current,
                                    @RequestParam(defaultValue = "10") long size) {
        LambdaQueryWrapper<BizComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizComment::getUserId, UserContext.getUserId())
                .orderByDesc(BizComment::getCreateTime);
        return R.ok(commentService.page(new Page<>(current, size), wrapper));
    }
}
