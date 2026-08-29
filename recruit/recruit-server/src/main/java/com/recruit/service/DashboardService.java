package com.recruit.service;

import com.recruit.vo.DashboardVO;

/**
 * 仪表盘服务。
 */
public interface DashboardService {

    DashboardVO hrDashboard();

    DashboardVO adminDashboard();
}
