package com.recruit.service.impl;

import com.recruit.context.BaseContext;
import com.recruit.entity.Notification;
import com.recruit.mapper.NotificationMapper;
import com.recruit.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 消息服务实现。
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public List<Notification> listCurrentUserNotifications() {
        return notificationMapper.listByUserId(BaseContext.getCurrentUserId());
    }

    @Override
    public List<Notification> listCurrentHrNotifications() {
        return notificationMapper.listForHr(BaseContext.getCurrentUserId());
    }

    @Override
    public void markRead(Long id) {
        notificationMapper.markRead(id, BaseContext.getCurrentUserId());
    }
}
