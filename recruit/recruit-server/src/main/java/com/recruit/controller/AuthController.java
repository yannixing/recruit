package com.recruit.controller;

import com.recruit.dto.CodeDTO;
import com.recruit.dto.LoginDTO;
import com.recruit.result.Result;
import com.recruit.service.AuthService;
import com.recruit.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口。
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/code")
    public Result<String> sendCode(@RequestBody CodeDTO codeDTO) {
        return Result.success(authService.sendCode(codeDTO));
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }
}
