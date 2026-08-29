package com.recruit.vo;

import com.recruit.entity.Interview;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 面试邀约展示信息。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InterviewVO extends Interview {

    private String jobTitle;
    private String companyName;
    private String candidateName;
}
