package com.recruit.mapper;

import com.recruit.entity.Delivery;
import com.recruit.vo.DeliveryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 投递记录数据访问接口。
 */
@Mapper
public interface DeliveryMapper {

    void insert(Delivery delivery);

    Long countByJobAndUser(@Param("jobId") Long jobId, @Param("userId") Long userId);

    Delivery getById(Long id);

    DeliveryVO getVoById(Long id);

    List<DeliveryVO> listByUserId(@Param("userId") Long userId, @Param("status") Integer status);

    List<DeliveryVO> listByHrId(@Param("hrId") Long hrId, @Param("jobId") Long jobId, @Param("status") Integer status);

    void updateStatus(Delivery delivery);
}
