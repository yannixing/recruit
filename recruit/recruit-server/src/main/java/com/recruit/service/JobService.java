package com.recruit.service;

import com.recruit.dto.JobDTO;
import com.recruit.dto.JobPageQueryDTO;
import com.recruit.dto.StatusUpdateDTO;
import com.recruit.result.PageResult;
import com.recruit.vo.JobVO;

import java.util.List;

/**
 * 职位服务。
 */
public interface JobService {

    void create(JobDTO jobDTO);

    void update(JobDTO jobDTO);

    void updateStatus(Long id, Integer status);

    void review(Long id, StatusUpdateDTO statusUpdateDTO);

    void delete(List<Long> ids);

    PageResult pageForHr(JobPageQueryDTO query);

    PageResult pageForAdmin(JobPageQueryDTO query);

    PageResult pageForAdminPendingReview(JobPageQueryDTO query);

    PageResult pageForCandidate(JobPageQueryDTO query);

    JobVO getForHr(Long id);

    JobVO getForAdmin(Long id);

    JobVO getForCandidate(Long id);

    List<JobVO> listHot();
}
