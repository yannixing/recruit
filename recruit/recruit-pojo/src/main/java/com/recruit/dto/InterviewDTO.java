package com.recruit.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * HR 创建或修改面试邀约的请求参数。
 */
@Data
public class InterviewDTO implements Serializable {

    /** 对应的投递记录 ID。 */
    private Long deliveryId;

    /** 面试官姓名。 */
    private String interviewer;

    /** 面试时间。 */
    private LocalDateTime interviewTime;

    /** 面试时长，单位为分钟。 */
    private Integer duration;

    /** 面试地点。 */
    private String location;

    /** 详细地址。 */
    private String addressDetail;

    /** 联系人。 */
    private String contactPerson;

    /** 联系电话。 */
    private String contactPhone;

    /** 面试备注。 */
    private String remark;
}
