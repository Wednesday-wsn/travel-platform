package com.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.entity.BizHotelRoom;
import com.travel.mapper.BizHotelRoomMapper;
import com.travel.service.HotelRoomService;
import org.springframework.stereotype.Service;

@Service
public class HotelRoomServiceImpl extends ServiceImpl<BizHotelRoomMapper, BizHotelRoom> implements HotelRoomService {
}
