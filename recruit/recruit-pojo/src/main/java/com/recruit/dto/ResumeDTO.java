package com.recruit.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 求职者创建或编辑简历的请求参数。
 */
@Data
public class ResumeDTO implements Serializable {

    private Long id;
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
}
