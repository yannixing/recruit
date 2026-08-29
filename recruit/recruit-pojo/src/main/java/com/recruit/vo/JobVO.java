package com.recruit.vo;

import com.recruit.entity.Job;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 职位展示信息。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JobVO extends Job {

    private String companyName;
    private String shortName;
    private String industry;
    private String companySize;
    private String logo;
    private String address;
    private String website;
    private String companyDescription;
}
