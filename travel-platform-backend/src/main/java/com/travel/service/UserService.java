package com.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.dto.LoginDTO;
import com.travel.dto.UserRegisterDTO;
import com.travel.entity.SysUser;
import com.travel.vo.LoginVO;

public interface UserService extends IService<SysUser> {

    LoginVO login(LoginDTO dto);

    void register(UserRegisterDTO dto);
}
