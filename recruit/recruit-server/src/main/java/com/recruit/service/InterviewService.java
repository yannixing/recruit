package com.recruit.service;

import com.recruit.dto.InterviewDTO;
import com.recruit.dto.StatusUpdateDTO;
import com.recruit.result.PageResult;

/**
 * 面试服务。
 */
public interface InterviewService {

    PageResult listForCandidate();

    PageResult listForHr();

    Long create(InterviewDTO interviewDTO);

    void update(Long id, InterviewDTO interviewDTO);

    void cancel(Long id, String remark);

    void updateStatusForHr(Long id, StatusUpdateDTO statusUpdateDTO);

    void updateStatusForCandidate(Long id, StatusUpdateDTO statusUpdateDTO);
}
