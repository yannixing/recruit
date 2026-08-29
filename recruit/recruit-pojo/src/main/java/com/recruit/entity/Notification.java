package com.recruit.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息通知，对应 notification 表。
 */
@Data
public class Notification implements Serializable {

    private Long id;
    private Long userId;
    private Integer type;
    private String title;
    private String content;
    private Long bizId;
    private Integer isRead;
    private LocalDateTime readTime;
    private LocalDateTime createTime;
}
