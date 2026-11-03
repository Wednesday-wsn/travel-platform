package com.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.common.exception.BusinessException;
import com.travel.common.result.ResultCode;
import com.travel.common.utils.JwtUtil;
import com.travel.dto.LoginDTO;
import com.travel.entity.SysAdmin;
import com.travel.enums.Role;
import com.travel.mapper.SysAdminMapper;
import com.travel.service.AdminService;
import com.travel.vo.LoginVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl extends ServiceImpl<SysAdminMapper, SysAdmin> implements AdminService {

    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminServiceImpl(JwtUtil jwtUtil, BCryptPasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        SysAdmin admin = lambdaQuery().eq(SysAdmin::getUsername, dto.getUsername()).one();
        if (admin == null || !passwordEncoder.matches(dto.getPassword(), admin.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_FAIL);
        }
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        String token = jwtUtil.createToken(admin.getId(), Role.ADMIN.getCode());
        return LoginVO.builder()
                .token(token)
                .id(admin.getId())
                .role(Role.ADMIN.getCode())
                .name(admin.getRealName())
                .username(admin.getUsername())
                .expireAt(jwtUtil.expireAtMillis())
                .build();
    }
}
