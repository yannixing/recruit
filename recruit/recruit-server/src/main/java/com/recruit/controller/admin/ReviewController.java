package com.recruit.controller.admin;

import com.recruit.dto.JobPageQueryDTO;
import com.recruit.dto.StatusUpdateDTO;
import com.recruit.result.PageResult;
import com.recruit.result.Result;
import com.recruit.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 职位审核接口。
 */
@RestController
@RequestMapping("/admin/reviews")
public class ReviewController {

    @Autowired
    private JobService jobService;

    @GetMapping
    public Result<PageResult> list(JobPageQueryDTO query) {
        return Result.success(jobService.pageForAdminPendingReview(query));
    }

    @PostMapping("/{id}")
    public Result<Void> review(@PathVariable Long id, @RequestBody StatusUpdateDTO statusUpdateDTO) {
        jobService.review(id, statusUpdateDTO);
        return Result.success();
    }
}
