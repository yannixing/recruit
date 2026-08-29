package com.recruit.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户，对应 user 表。
 */
@Data
public class User implements Serializable {

    private Long id;
    private String username;
    private String password;
    private String phone;
    private String email;
    private Integer role;
    private String avatar;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
