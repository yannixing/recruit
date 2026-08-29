package com.recruit.mapper;

import com.recruit.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息通知数据访问接口。
 */
@Mapper
public interface NotificationMapper {

    void insert(Notification notification);

    List<Notification> listByUserId(@Param("userId") Long userId);

    List<Notification> listForHr(@Param("hrId") Long hrId);

    void markRead(@Param("id") Long id, @Param("userId") Long userId);
}
