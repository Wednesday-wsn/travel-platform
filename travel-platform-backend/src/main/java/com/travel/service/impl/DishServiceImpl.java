package com.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.entity.BizDish;
import com.travel.mapper.BizDishMapper;
import com.travel.service.DishService;
import org.springframework.stereotype.Service;

@Service
public class DishServiceImpl extends ServiceImpl<BizDishMapper, BizDish> implements DishService {
}
