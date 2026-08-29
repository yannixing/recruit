package com.recruit.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 仪表盘统计数据访问接口。
 */
@Mapper
public interface DashboardMapper {

    Long countAllJobs(@Param("hrId") Long hrId);

    Long countActiveJobs(@Param("hrId") Long hrId);

    Long countPendingJobs(@Param("hrId") Long hrId);

    Long countDeliveries(@Param("hrId") Long hrId);

    Long countInterviews(@Param("hrId") Long hrId);

    Long countUsers();

    Long countUsersByRole(@Param("role") Integer role);
}
