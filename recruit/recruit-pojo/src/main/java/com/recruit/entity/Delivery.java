package com.recruit.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 投递记录，对应 delivery 表。
 */
@Data
public class Delivery implements Serializable {

    private Long id;
    private Long jobId;
    private Long userId;
    private Long resumeId;
    private Integer status;
    private String statusRemark;
    private LocalDateTime deliveryTime;
    private LocalDateTime viewTime;
    private LocalDateTime interviewTime;
    private LocalDateTime offerTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
