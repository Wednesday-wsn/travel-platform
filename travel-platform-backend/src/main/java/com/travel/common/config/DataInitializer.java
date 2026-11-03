package com.travel.common.config;

import com.travel.entity.SysAdmin;
import com.travel.service.AdminService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 启动时初始化：无管理员时自动创建默认管理员账号 admin / admin123
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final AdminService adminService;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(AdminService adminService, BCryptPasswordEncoder passwordEncoder) {
        this.adminService = adminService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminService.count() == 0) {
            SysAdmin admin = new SysAdmin();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRealName("系统管理员");
            admin.setStatus(1);
            adminService.save(admin);
        }
    }
}
