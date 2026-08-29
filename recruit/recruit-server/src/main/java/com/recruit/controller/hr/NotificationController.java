package com.recruit.controller.hr;

import com.recruit.entity.Notification;
import com.recruit.result.Result;
import com.recruit.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * HR 通知接口。
 */
@RestController("hrnotification")
@RequestMapping("/hr/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public Result<List<Notification>> list() {
        return Result.success(notificationService.listCurrentHrNotifications());
    }

    @PutMapping("/{id}/read")
    public Result<Void> read(@PathVariable Long id) {
        notificationService.markRead(id);
        return Result.success();
    }
}
