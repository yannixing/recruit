package com.recruit.controller.candidate;

import com.recruit.dto.StatusUpdateDTO;
import com.recruit.result.PageResult;
import com.recruit.result.Result;
import com.recruit.service.InterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 求职者面试邀约接口。
 */
@RestController("candidateinterview")
@RequestMapping("/candidate/interviews")
public class InterviewController {

    @Autowired
    private InterviewService interviewService;

    @GetMapping
    public Result<PageResult> list() {
        return Result.success(interviewService.listForCandidate());
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestBody StatusUpdateDTO statusUpdateDTO) {
        interviewService.updateStatusForCandidate(id, statusUpdateDTO);
        return Result.success();
    }
}
