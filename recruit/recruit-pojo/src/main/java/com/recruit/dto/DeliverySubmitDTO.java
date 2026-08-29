package com.recruit.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 求职者投递简历请求。
 */
@Data
public class DeliverySubmitDTO implements Serializable {

    private Long jobId;
    private Long resumeId;
}
