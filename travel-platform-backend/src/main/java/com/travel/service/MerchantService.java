package com.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.dto.LoginDTO;
import com.travel.dto.MerchantRegisterDTO;
import com.travel.entity.BizMerchant;
import com.travel.vo.LoginVO;

public interface MerchantService extends IService<BizMerchant> {

    LoginVO login(LoginDTO dto);

    void register(MerchantRegisterDTO dto);
}
