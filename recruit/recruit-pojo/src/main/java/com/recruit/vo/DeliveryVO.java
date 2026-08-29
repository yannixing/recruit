package com.recruit.vo;

import com.recruit.entity.Delivery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 投递记录展示信息。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeliveryVO extends Delivery {

    private String jobTitle;
    private String companyName;
    private Integer salaryMin;
    private Integer salaryMax;
    private String city;
    private String resumeName;
    private String candidateName;
    private String candidatePhone;
    private String candidateEmail;
    private String education;
    private String school;
    private String skill;
}
