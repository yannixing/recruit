package com.recruit.mapper;

import com.github.pagehelper.Page;
import com.recruit.annotation.AutoFill;
import com.recruit.dto.JobPageQueryDTO;
import com.recruit.entity.Job;
import com.recruit.enumeration.OperationType;
import com.recruit.vo.JobVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 职位数据访问接口。
 */
@Mapper
public interface JobMapper {

    @AutoFill(OperationType.INSERT)
    void insert(Job job);

    @AutoFill(OperationType.UPDATE)
    void update(Job job);

    Job getById(Long id);

    JobVO getVoById(Long id);

    Page<JobVO> pageQuery(JobPageQueryDTO query);

    List<JobVO> listHot();

    Long countDeliveriesByJobId(Long jobId);

    void refreshDeliveryCount(@Param("jobId") Long jobId);

    void incrementViewCount(@Param("jobId") Long jobId);

    void review(@Param("id") Long id, @Param("status") Integer status, @Param("auditRemark") String auditRemark);

    void deleteByIds(@Param("ids") List<Long> ids);
}
