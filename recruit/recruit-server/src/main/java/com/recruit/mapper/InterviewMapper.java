package com.recruit.mapper;

import com.recruit.entity.Interview;
import com.recruit.vo.InterviewVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 面试邀约数据访问接口。
 */
@Mapper
public interface InterviewMapper {

    void insert(Interview interview);

    Interview getById(Long id);

    List<InterviewVO> listByUserId(@Param("userId") Long userId);

    List<InterviewVO> listByHrId(@Param("hrId") Long hrId);

    InterviewVO getVoById(Long id);

    void update(Interview interview);

    void updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("feedback") String feedback);
}
