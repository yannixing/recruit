package com.recruit.controller.hr;

import com.recruit.dto.StatusUpdateDTO;
import com.recruit.result.PageResult;
import com.recruit.result.Result;
import com.recruit.service.DeliveryService;
import com.recruit.vo.DeliveryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * HR 投递管理接口。
 */
@RestController("hrdelivery")
@RequestMapping("/hr/applications")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @GetMapping
    public Result<PageResult> list(@RequestParam(required = false) Long jobId,
                                   @RequestParam(required = false) Integer status) {
        return Result.success(deliveryService.listForHr(jobId, status));
    }

    @GetMapping("/{id}")
    public Result<DeliveryVO> getById(@PathVariable Long id) {
        return Result.success(deliveryService.getForHr(id));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestBody StatusUpdateDTO statusUpdateDTO) {
        deliveryService.updateStatusForHr(id, statusUpdateDTO);
        return Result.success();
    }
}
