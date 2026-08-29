package com.recruit.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 面试邀约，对应 interview 表。
 */
@Data
public class Interview implements Serializable {

    private Long id;
    private Long deliveryId;
    private Long hrId;
    private String interviewer;
    private LocalDateTime interviewTime;
    private Integer duration;
    private String location;
    private String addressDetail;
    private String contactPerson;
    private String contactPhone;
    private String remark;
    private Integer status;
    private String feedback;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
