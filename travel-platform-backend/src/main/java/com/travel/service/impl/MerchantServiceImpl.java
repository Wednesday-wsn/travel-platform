package com.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.common.exception.BusinessException;
import com.travel.common.result.ResultCode;
import com.travel.common.utils.JwtUtil;
import com.travel.dto.LoginDTO;
import com.travel.dto.MerchantRegisterDTO;
import com.travel.entity.BizMerchant;
import com.travel.enums.Role;
import com.travel.mapper.BizMerchantMapper;
import com.travel.service.MerchantService;
import com.travel.vo.LoginVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MerchantServiceImpl extends ServiceImpl<BizMerchantMapper, BizMerchant> implements MerchantService {

    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public MerchantServiceImpl(JwtUtil jwtUtil, BCryptPasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        BizMerchant merchant = lambdaQuery().eq(BizMerchant::getUsername, dto.getUsername()).one();
        if (merchant == null || !passwordEncoder.matches(dto.getPassword(), merchant.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_FAIL);
        }
        // 禁用状态不允许登录；待审核/驳回允许登录（前端展示对应状态）
        if (merchant.getStatus() != null && merchant.getStatus() == 3) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        String token = jwtUtil.createToken(merchant.getId(), Role.MERCHANT.getCode());
        return LoginVO.builder()
                .token(token)
                .id(merchant.getId())
                .role(Role.MERCHANT.getCode())
                .name(merchant.getName())
                .username(merchant.getUsername())
                .avatar(merchant.getLogo())
                .expireAt(jwtUtil.expireAtMillis())
                .build();
    }

    @Override
    public void register(MerchantRegisterDTO dto) {
        Long count = lambdaQuery().eq(BizMerchant::getUsername, dto.getUsername()).count();
        if (count != null && count > 0) {
            throw new BusinessException(ResultCode.ACCOUNT_EXISTS);
        }
        BizMerchant merchant = new BizMerchant();
        merchant.setUsername(dto.getUsername());
        merchant.setPassword(passwordEncoder.encode(dto.getPassword()));
        merchant.setName(dto.getName());
        merchant.setType(dto.getType());
        merchant.setContactName(dto.getContactName());
        merchant.setContactPhone(dto.getContactPhone());
        merchant.setEmail(dto.getEmail());
        merchant.setAddress(dto.getAddress());
        merchant.setDescription(dto.getDescription());
        merchant.setLogo(dto.getLogo());
        merchant.setStatus(0); // 待审核
        save(merchant);
    }
}
