package com.recruit.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 工作台统计数据。
 */
@Data
@Builder
public class DashboardVO implements Serializable {

    /** 平台全部职位总数 */
    private Long totalJobs;

    private Long activeJobs;
    private Long pendingJobs;
    private Long deliveries;
    private Long interviews;
    private Long users;
    private Long candidates;
    private Long hrs;
}
