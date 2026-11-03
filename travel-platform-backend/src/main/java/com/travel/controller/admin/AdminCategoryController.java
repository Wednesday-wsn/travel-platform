package com.travel.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.common.result.R;
import com.travel.entity.BizCategory;
import com.travel.service.CategoryService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端：分类管理
 */
@RestController
@RequestMapping("/api/admin/category")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/list")
    public R<List<BizCategory>> list(@RequestParam(required = false) Integer type) {
        LambdaQueryWrapper<BizCategory> wrapper = new LambdaQueryWrapper<>();
        if (type != null) {
            wrapper.eq(BizCategory::getType, type);
        }
        wrapper.orderByAsc(BizCategory::getSort);
        return R.ok(categoryService.list(wrapper));
}

@PostMapping
public R<Void> create(@RequestBody BizCategory category) {
    category.setId(null);
    categoryService.save(category);
    return R.ok();
}

    @PutMapping
    public R<Void> update(@RequestBody BizCategory category) {
        categoryService.updateById(category);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        categoryService.removeById(id);
        return R.ok();
    }
}
