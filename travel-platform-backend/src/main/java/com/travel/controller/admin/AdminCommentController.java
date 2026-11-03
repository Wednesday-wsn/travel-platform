package com.travel.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.result.R;
import com.travel.service.CommentService;
import com.travel.vo.CommentVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端：评价管理（审核走统一审核接口）
 */
@RestController
@RequestMapping("/api/admin/comment")
public class AdminCommentController {

    private final CommentService commentService;

    public AdminCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/page")
    public R<Page<CommentVO>> page(@RequestParam(defaultValue = "1") long current,
                                    @RequestParam(defaultValue = "10") long size,
                                    @RequestParam(required = false) Integer status,
                                    @RequestParam(required = false) Integer targetType) {
        return R.ok(commentService.pageAdmin(current, size, status, targetType));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        commentService.removeById(id);
        return R.ok();
    }
}
