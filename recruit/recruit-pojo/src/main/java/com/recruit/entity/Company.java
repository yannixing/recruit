package com.recruit.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企业信息，对应 company 表。
 */
@Data
public class Company implements Serializable {

    private Long id;
    private Long hrId;
    private String companyName;
    private String shortName;
    private String industry;
    private String companySize;
    private String logo;
    private String address;
    private String website;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
