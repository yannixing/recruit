package com.recruit.controller.admin;

import com.recruit.result.Result;
import com.recruit.service.DashboardService;
import com.recruit.vo.DashboardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员仪表盘接口。
 */
@RestController("admindashboard")
@RequestMapping("/admin/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    public Result<DashboardVO> dashboard() {
        return Result.success(dashboardService.adminDashboard());
    }
}
