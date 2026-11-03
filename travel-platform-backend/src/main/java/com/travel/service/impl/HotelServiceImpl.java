package com.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.entity.BizHotel;
import com.travel.mapper.BizHotelMapper;
import com.travel.service.HotelService;
import org.springframework.stereotype.Service;

@Service
public class HotelServiceImpl extends ServiceImpl<BizHotelMapper, BizHotel> implements HotelService {
}
