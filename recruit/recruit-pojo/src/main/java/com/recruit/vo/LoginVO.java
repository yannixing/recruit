package com.recruit.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录成功返回信息。
 */
@Data
@Builder
public class LoginVO implements Serializable {

    private Long userId;
    private String username;
    private Integer role;
    private String token;
}
