package com.recruit.interceptor;

import com.recruit.constant.JwtClaimsConstant;
import com.recruit.constant.RoleConstant;
import com.recruit.context.BaseContext;
import com.recruit.properties.JwtProperties;
import com.recruit.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT 认证与角色校验拦截器。
 */
@Slf4j
@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader(jwtProperties.getTokenName());
        try {
            Claims claims = JwtUtil.parseJwt(jwtProperties.getSecretKey(), token);
            Long userId = Long.valueOf(String.valueOf(claims.get(JwtClaimsConstant.USER_ID)));
            Integer role = Integer.valueOf(String.valueOf(claims.get(JwtClaimsConstant.ROLE)));
            String path = request.getRequestURI();

            if (path.startsWith("/hr/") && role != RoleConstant.HR) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
            if (path.startsWith("/candidate/") && role != RoleConstant.CANDIDATE) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
            if (path.startsWith("/admin/") && role != RoleConstant.ADMIN) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }

            BaseContext.setCurrentUserId(userId);
            return true;
        } catch (Exception ex) {
            log.debug("JWT 校验失败", ex);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        BaseContext.removeCurrentUserId();
    }
}
