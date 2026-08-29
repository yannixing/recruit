package com.recruit.controller.hr;

import com.recruit.dto.JobDTO;
import com.recruit.dto.JobPageQueryDTO;
import com.recruit.result.PageResult;
import com.recruit.result.Result;
import com.recruit.service.JobService;
import com.recruit.vo.JobVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * HR 职位管理接口。
 */
@RestController("hrjob")
@RequestMapping("/hr/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @PostMapping
    public Result<Void> create(@RequestBody JobDTO jobDTO) {
        jobService.create(jobDTO);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody JobDTO jobDTO) {
        jobService.update(jobDTO);
        return Result.success();
    }

    @PostMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        jobService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping
    public Result<Void> delete(@RequestParam List<Long> ids) {
        jobService.delete(ids);
        return Result.success();
    }

    @GetMapping
    public Result<PageResult> page(JobPageQueryDTO query) {
        return Result.success(jobService.pageForHr(query));
    }

    @GetMapping("/page")
    public Result<PageResult> pageAlias(JobPageQueryDTO query) {
        return Result.success(jobService.pageForHr(query));
    }

    @GetMapping("/{id}")
    public Result<JobVO> getById(@PathVariable Long id) {
        return Result.success(jobService.getForHr(id));
    }
}
