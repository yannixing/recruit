package com.recruit.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 简历，对应 resume 表。
 */
@Data
public class Resume implements Serializable {

    private Long id;
    private Long userId;
    private String name;
    private Integer gender;
    private LocalDate birthDate;
    private String phone;
    private String email;
    private String address;
    private String education;
    private String school;
    private String major;
    private String workExperience;
    private String projectExperience;
    private String skill;
    private String selfEvaluation;
    private String attachment;
    private Integer isDefault;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
