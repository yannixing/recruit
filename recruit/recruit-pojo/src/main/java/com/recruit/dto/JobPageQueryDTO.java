package com.recruit.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 职位分页和搜索条件。
 */
@Data
public class JobPageQueryDTO implements Serializable {

    private int page = 1;
    private int pageSize = 10;
    private String keyword;//职位关键词
    private String city;
    private String category;//职位分类
    private Integer salaryMin;//最低薪水
    private Integer salaryMax;
    private Integer status;
    private Long hrId;
}
