package com.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.entity.BizCategory;
import com.travel.mapper.BizCategoryMapper;
import com.travel.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<BizCategoryMapper, BizCategory> implements CategoryService {
}
