package com.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.entity.BizSpotTicket;
import com.travel.mapper.BizSpotTicketMapper;
import com.travel.service.SpotTicketService;
import org.springframework.stereotype.Service;

@Service
public class SpotTicketServiceImpl extends ServiceImpl<BizSpotTicketMapper, BizSpotTicket> implements SpotTicketService {
}
