package com.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.entity.BizFavorite;
import com.travel.mapper.BizFavoriteMapper;
import com.travel.service.FavoriteService;
import org.springframework.stereotype.Service;

@Service
public class FavoriteServiceImpl extends ServiceImpl<BizFavoriteMapper, BizFavorite> implements FavoriteService {
}
