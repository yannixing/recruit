package com.recruit.service;

import com.recruit.dto.DeliverySubmitDTO;
import com.recruit.dto.StatusUpdateDTO;
import com.recruit.result.PageResult;
import com.recruit.vo.DeliveryVO;

/**
 * 投递服务。
 */
public interface DeliveryService {

    void submit(DeliverySubmitDTO deliverySubmitDTO);

    PageResult listForCandidate(Integer status);

    PageResult listForHr(Long jobId, Integer status);

    DeliveryVO getForCandidate(Long id);

    DeliveryVO getForHr(Long id);

    void updateStatusForHr(Long id, StatusUpdateDTO statusUpdateDTO);
}
