package com.recruit.service.impl;

import com.recruit.constant.RoleConstant;
import com.recruit.mapper.DashboardMapper;
import com.recruit.service.DashboardService;
import com.recruit.vo.DashboardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 仪表盘服务实现。
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardMapper dashboardMapper;

    @Override
    public DashboardVO hrDashboard() {
        return DashboardVO.builder()
                .totalJobs(dashboardMapper.countAllJobs(com.recruit.context.BaseContext.getCurrentUserId()))
                .activeJobs(dashboardMapper.countActiveJobs(com.recruit.context.BaseContext.getCurrentUserId()))
                .pendingJobs(dashboardMapper.countPendingJobs(com.recruit.context.BaseContext.getCurrentUserId()))
                .deliveries(dashboardMapper.countDeliveries(com.recruit.context.BaseContext.getCurrentUserId()))
                .interviews(dashboardMapper.countInterviews(com.recruit.context.BaseContext.getCurrentUserId()))
                .users(dashboardMapper.countUsers())
                .candidates(dashboardMapper.countUsersByRole(RoleConstant.CANDIDATE))
                .hrs(dashboardMapper.countUsersByRole(RoleConstant.HR))
                .build();
    }

    @Override
    public DashboardVO adminDashboard() {
        return DashboardVO.builder()//为null表示不限制hrid 查询所有
                .totalJobs(dashboardMapper.countAllJobs(null))
                .activeJobs(dashboardMapper.countActiveJobs(null))
                .pendingJobs(dashboardMapper.countPendingJobs(null))
                .deliveries(dashboardMapper.countDeliveries(null))
                .interviews(dashboardMapper.countInterviews(null))
                .users(dashboardMapper.countUsers())
                .candidates(dashboardMapper.countUsersByRole(RoleConstant.CANDIDATE))
                .hrs(dashboardMapper.countUsersByRole(RoleConstant.HR))
                .build();
    }
}
