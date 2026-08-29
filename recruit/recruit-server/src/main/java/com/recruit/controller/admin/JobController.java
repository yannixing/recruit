package com.recruit.controller.admin;

import com.recruit.dto.JobPageQueryDTO;
import com.recruit.result.PageResult;
import com.recruit.result.Result;
import com.recruit.service.JobService;
import com.recruit.vo.JobVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员职位管理接口。
 *
 * 管理员职位管理展示全部岗位，职位审核使用 ReviewController 单独查询待审核岗位。
 */
@RestController("adminjob")
@RequestMapping("/admin/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    /**
     * 分页查询全部岗位。
     */
    @GetMapping
    public Result<PageResult> page(JobPageQueryDTO query) {
        return Result.success(jobService.pageForAdmin(query));
    }

    /**
     * 查看职位完整信息。
     */
    @GetMapping("/{id}")
    public Result<JobVO> getById(@PathVariable Long id) {
        return Result.success(jobService.getForAdmin(id));
    }
}
