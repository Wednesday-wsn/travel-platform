package com.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.entity.BizScenicSpot;
import com.travel.mapper.BizScenicSpotMapper;
import com.travel.service.ScenicSpotService;
import org.springframework.stereotype.Service;

@Service
public class ScenicSpotServiceImpl extends ServiceImpl<BizScenicSpotMapper, BizScenicSpot> implements ScenicSpotService {
}
