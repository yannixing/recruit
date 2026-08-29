package com.recruit.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 获取验证码请求。
 */
@Data
public class CodeDTO implements Serializable {

    private String account;
    private Integer role;
}
