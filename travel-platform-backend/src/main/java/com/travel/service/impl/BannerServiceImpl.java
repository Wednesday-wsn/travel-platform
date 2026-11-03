package com.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.entity.BizBanner;
import com.travel.mapper.BizBannerMapper;
import com.travel.service.BannerService;
import org.springframework.stereotype.Service;

@Service
public class BannerServiceImpl extends ServiceImpl<BizBannerMapper, BizBanner> implements BannerService {
}
