package com.recruit.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * HR 创建或编辑职位的请求参数。
 */
@Data
public class JobDTO implements Serializable {

    private Long id;
    private Long companyId;
    private String title;
    private String category;
    private Integer salaryMin;
    private Integer salaryMax;
    private String city;
    private String workExperience;
    private String education;
    private String jobDescription;
    private String requirement;
    private String benefits;
    private Integer status;
}
