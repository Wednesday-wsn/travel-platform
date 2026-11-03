package com.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.entity.BizRestaurant;
import com.travel.mapper.BizRestaurantMapper;
import com.travel.service.RestaurantService;
import org.springframework.stereotype.Service;

@Service
public class RestaurantServiceImpl extends ServiceImpl<BizRestaurantMapper, BizRestaurant> implements RestaurantService {
}
