package com.shogun.signalling.bean;

import lombok.Data;

import java.util.Map;

@Data
public class NotificationBean {
    private String title;
    private String body;
    private Integer notificationUserId;
    private Map<String, String> data;
}
