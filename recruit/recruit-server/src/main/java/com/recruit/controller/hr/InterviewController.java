package com.recruit.controller.hr;

import com.recruit.dto.InterviewDTO;
import com.recruit.dto.StatusUpdateDTO;
import com.recruit.result.PageResult;
import com.recruit.result.Result;
import com.recruit.service.InterviewService;
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

/**
 * HR 面试邀约接口。
 */
@RestController("hrinterview")
@RequestMapping("/hr/interviews")
public class InterviewController {

    @Autowired
    private InterviewService interviewService;

    @GetMapping
    public Result<PageResult> list() {
        return Result.success(interviewService.listForHr());
    }

    @PostMapping
    public Result<Long> create(@RequestBody InterviewDTO interviewDTO) {
        return Result.success(interviewService.create(interviewDTO));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody InterviewDTO interviewDTO) {
        interviewService.update(id, interviewDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> cancel(@PathVariable Long id,
                               @RequestParam(required = false) String remark) {
        interviewService.cancel(id, remark);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestBody StatusUpdateDTO statusUpdateDTO) {
        interviewService.updateStatusForHr(id, statusUpdateDTO);
        return Result.success();
    }
}
