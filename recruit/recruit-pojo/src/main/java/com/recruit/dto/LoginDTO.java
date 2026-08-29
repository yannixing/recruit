package com.recruit.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 验证码登录请求。
 */
@Data
public class LoginDTO implements Serializable {

    private String account;
    private String code;
    private Integer role;
}
