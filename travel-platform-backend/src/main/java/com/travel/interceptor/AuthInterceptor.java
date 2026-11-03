package com.travel.interceptor;

import com.travel.common.context.LoginUser;
import com.travel.common.context.UserContext;
import com.travel.common.result.ResultCode;
import com.travel.common.utils.JwtUtil;
import com.travel.enums.Role;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录认证与角色鉴权拦截器
 * <p>
 * 规则：/api/public/** 无需登录；/api/user/** 需 user 角色；
 * /api/merchant/** 需 merchant 角色；/api/admin/** 需 admin 角色；
 * /api/common/** 任意已登录角色均可访问。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 放行预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return writeError(response, ResultCode.UNAUTHORIZED);
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            Claims claims = jwtUtil.parseToken(token);
            Long userId = Long.valueOf(claims.getSubject());
            String role = claims.get("role", String.class);
            UserContext.set(new LoginUser(userId, role));

            String uri = request.getRequestURI();
            if (!checkRole(uri, role)) {
                return writeError(response, ResultCode.FORBIDDEN);
            }
            return true;
        } catch (Exception e) {
            return writeError(response, ResultCode.UNAUTHORIZED);
        }
    }

    private boolean checkRole(String uri, String role) {
        if (uri.startsWith("/api/admin/") && !Role.ADMIN.getCode().equals(role)) {
            return false;
        }
        if (uri.startsWith("/api/merchant/") && !Role.MERCHANT.getCode().equals(role)) {
            return false;
        }
        if (uri.startsWith("/api/user/") && !Role.USER.getCode().equals(role)) {
            return false;
        }
        return true;
    }

    private boolean writeError(HttpServletResponse response, ResultCode rc) {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write(String.format(
                    "{\"code\":%d,\"message\":\"%s\",\"data\":null}", rc.getCode(), rc.getMessage()));
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
