package com.shogun.signalling.bean;


import com.shogun.signalling.enumeration.CallingMessageType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomMessageBean {
    private AuthorBean author;
    private long createdAt;
    private String roomId;
    private MetadataBean metadata;
    private boolean needNotify;

    public static CustomMessageBean create(CallNotificationBean notificationBean, Long millisecondsSinceEpoch, CallingMessageType callingMessageType, Long duration, boolean needNotify) {
        AuthorBean author = new AuthorBean(notificationBean.getCallerId().toString(), notificationBean.getIcon(), notificationBean.getCallerName());
        MetadataBean metadata = new MetadataBean(callingMessageType.getValue(), duration);
        return new CustomMessageBean(author, millisecondsSinceEpoch, notificationBean.getPartnerId().toString(), metadata, needNotify);
    }
}

