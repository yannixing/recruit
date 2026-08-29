package com.recruit.controller.candidate;

import com.recruit.dto.DeliverySubmitDTO;
import com.recruit.result.PageResult;
import com.recruit.result.Result;
import com.recruit.service.DeliveryService;
import com.recruit.vo.DeliveryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 求职者投递接口。
 */
@RestController("candidatedelivery")
@RequestMapping("/candidate/deliveries")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @GetMapping
    public Result<PageResult> list(@RequestParam(required = false) Integer status) {
        return Result.success(deliveryService.listForCandidate(status));
    }

    @GetMapping("/{id}")
    public Result<DeliveryVO> getById(@PathVariable Long id) {
        return Result.success(deliveryService.getForCandidate(id));
    }

    @PostMapping
    public Result<Void> submit(@RequestBody DeliverySubmitDTO deliverySubmitDTO) {
        deliveryService.submit(deliverySubmitDTO);
        return Result.success();
    }
}
