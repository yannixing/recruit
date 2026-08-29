package com.recruit.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 职位，对应 job 表。
 */
@Data
public class Job implements Serializable {

    private Long id;
    private Long companyId;
    private Long hrId;
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
    private Integer deliveryCount;
    private Integer viewCount;
    private Integer status;
    private String auditRemark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
