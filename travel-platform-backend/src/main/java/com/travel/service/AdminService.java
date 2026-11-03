package com.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.dto.LoginDTO;
import com.travel.entity.SysAdmin;
import com.travel.vo.LoginVO;

public interface AdminService extends IService<SysAdmin> {

    LoginVO login(LoginDTO dto);
}
