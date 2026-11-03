package com.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.entity.BizNotice;
import com.travel.mapper.BizNoticeMapper;
import com.travel.service.NoticeService;
import org.springframework.stereotype.Service;

@Service
public class NoticeServiceImpl extends ServiceImpl<BizNoticeMapper, BizNotice> implements NoticeService {
}
