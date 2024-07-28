package com.shogun.signalling.bean;

import com.shogun.signalling.enumeration.NotificationType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CallNotificationBean {
    private NotificationType type;
    private String callerName;
    private Integer callerId;
    private Integer partnerId;
    private String icon;
    private boolean isVideoCalling;
    private boolean needNotify;
}
