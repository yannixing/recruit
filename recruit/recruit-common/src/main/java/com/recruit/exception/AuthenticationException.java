package com.recruit.exception;

/**
 * 登录认证失败异常。
 */
public class AuthenticationException extends BaseException {

    public AuthenticationException(String message) {
        super(message);
    }
}
