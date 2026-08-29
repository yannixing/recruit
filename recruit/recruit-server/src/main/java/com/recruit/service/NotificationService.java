package com.recruit.service;

import com.recruit.entity.Notification;

import java.util.List;

/**
 * 通知服务。
 */
public interface NotificationService {

    List<Notification> listCurrentUserNotifications();

    List<Notification> listCurrentHrNotifications();

    void markRead(Long id);
}
