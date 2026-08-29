package com.recruit.service;

import com.recruit.dto.CodeDTO;
import com.recruit.dto.LoginDTO;
import com.recruit.vo.LoginVO;

/**
 * 认证服务。
 */
public interface AuthService {

    String sendCode(CodeDTO codeDTO);

    LoginVO login(LoginDTO loginDTO);
}
