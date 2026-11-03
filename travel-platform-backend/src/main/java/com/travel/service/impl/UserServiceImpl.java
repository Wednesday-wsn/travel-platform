package com.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.common.exception.BusinessException;
import com.travel.common.result.ResultCode;
import com.travel.common.utils.JwtUtil;
import com.travel.dto.LoginDTO;
import com.travel.dto.UserRegisterDTO;
import com.travel.entity.SysUser;
import com.travel.enums.Role;
import com.travel.mapper.SysUserMapper;
import com.travel.service.UserService;
import com.travel.vo.LoginVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(JwtUtil jwtUtil, BCryptPasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        SysUser user = lambdaQuery().eq(SysUser::getUsername, dto.getUsername()).one();
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_FAIL);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        String token = jwtUtil.createToken(user.getId(), Role.USER.getCode());
        return LoginVO.builder()
                .token(token)
                .id(user.getId())
                .role(Role.USER.getCode())
                .name(user.getNickname())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .expireAt(jwtUtil.expireAtMillis())
                .build();
    }

    @Override
    public void register(UserRegisterDTO dto) {
        Long count = lambdaQuery().eq(SysUser::getUsername, dto.getUsername()).count();
        if (count != null && count > 0) {
            throw new BusinessException(ResultCode.ACCOUNT_EXISTS);
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() == null || dto.getNickname().isEmpty()
                ? dto.getUsername() : dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setGender(0);
        user.setStatus(1);
        save(user);
    }
}
