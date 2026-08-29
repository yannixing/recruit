package com.recruit.controller.candidate;

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

import java.util.List;

/**
 * 求职者职位接口。
 */
@RestController("candidatejob")
@RequestMapping("/candidate/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @GetMapping
    public Result<PageResult> page(JobPageQueryDTO query) {
        return Result.success(jobService.pageForCandidate(query));
    }

    @GetMapping("/hot")
    public Result<List<JobVO>> hot() {
        return Result.success(jobService.listHot());
    }

    @GetMapping("/{id}")
    public Result<JobVO> getById(@PathVariable Long id) {
        return Result.success(jobService.getForCandidate(id));
    }
}
