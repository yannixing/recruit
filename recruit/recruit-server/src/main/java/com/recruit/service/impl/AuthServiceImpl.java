package com.recruit.service.impl;

import com.recruit.constant.JwtClaimsConstant;
import com.recruit.dto.CodeDTO;
import com.recruit.dto.LoginDTO;
import com.recruit.entity.User;
import com.recruit.exception.AuthenticationException;
import com.recruit.mapper.UserMapper;
import com.recruit.properties.JwtProperties;
import com.recruit.service.AuthService;
import com.recruit.utils.JwtUtil;
import com.recruit.vo.LoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 验证码登录服务。
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private static final String LOGIN_CODE_PREFIX = "login:code:";
    private static final String DEV_CODE = "123456";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public String sendCode(CodeDTO codeDTO) {
        validateAccountAndRole(codeDTO.getAccount(), codeDTO.getRole());
        String code = DEV_CODE;
        try {
            redisTemplate.opsForValue().set(buildCodeKey(codeDTO.getAccount(), codeDTO.getRole()), code, 5, TimeUnit.MINUTES);
        } catch (Exception ex) {
            log.warn("验证码写入 Redis 失败，当前请求可继续使用开发验证码", ex);
        }
        return code;
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        validateAccountAndRole(loginDTO.getAccount(), loginDTO.getRole());
        if (!StringUtils.hasText(loginDTO.getCode())) {
            throw new AuthenticationException("验证码不能为空");
        }

        verifyCode(loginDTO.getAccount(), loginDTO.getRole(), loginDTO.getCode());

        User user = userMapper.getByAccountAndRole(loginDTO.getAccount(), loginDTO.getRole());
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new AuthenticationException("账号不存在、角色不匹配或已被禁用");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        claims.put(JwtClaimsConstant.ROLE, user.getRole());
        String token = JwtUtil.createJwt(jwtProperties.getSecretKey(), jwtProperties.getTtl(), claims);

        return LoginVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .token(token)
                .build();
    }

    private void verifyCode(String account, Integer role, String code) {
        String key = buildCodeKey(account, role);
        Object cachedCode = null;
        try {
            cachedCode = redisTemplate.opsForValue().get(key);
        } catch (Exception ex) {
            log.warn("验证码读取 Redis 失败，尝试使用开发验证码兜底", ex);
        }

        if (cachedCode == null) {
            if (!DEV_CODE.equals(code)) {
                throw new AuthenticationException("验证码错误或已过期");
            }
            return;
        }

        if (!String.valueOf(cachedCode).equals(code)) {
            throw new AuthenticationException("验证码错误");
        }
        try {
            // 验证码校验成功后删除，避免同一验证码重复使用。
            redisTemplate.delete(key);
        } catch (Exception ex) {
            // 删除失败不影响本次登录，验证码会在过期时间到达后自动清理。
        }
    }

    private void validateAccountAndRole(String account, Integer role) {
        if (!StringUtils.hasText(account) || role == null) {
            throw new AuthenticationException("账号和角色不能为空");
        }
    }

    private String buildCodeKey(String account, Integer role) {
        return LOGIN_CODE_PREFIX + role + ":" + account;
    }
}
