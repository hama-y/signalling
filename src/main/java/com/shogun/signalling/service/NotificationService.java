package com.shogun.signalling.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shogun.signalling.bean.CallNotificationBean;
import com.shogun.signalling.bean.NotificationBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    @Autowired
    @Qualifier("notifyWebClient")
    private WebClient webClient;
    private final ObjectMapper objectMapper;

    public void sendNotification(CallNotificationBean bean) throws JsonProcessingException {
        NotificationBean notificationBean = new NotificationBean();
        // タイトルは名前
        notificationBean.setTitle(bean.getCallerName());
        // 通知先ユーザーID
        notificationBean.setNotificationUserId(bean.getPartnerId());
        // 通知内容はなし
        notificationBean.setBody("");
        // 通知データ格納
        Map<String, Object> dataDetails = new HashMap<>();
        dataDetails.put("icon", bean.getIcon());
        dataDetails.put("name", bean.getCallerName());
        dataDetails.put("id", bean.getCallerId());
        dataDetails.put("isVideoCalling", bean.isVideoCalling());


        Map<String, String> data = new HashMap<>();
        data.put("type", bean.getType().toString());
        data.put("data", objectMapper.writeValueAsString(dataDetails));
        notificationBean.setData(data);


        // 通知基盤にPUSH通知してもらう
        webClient.post()
                .uri("/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(notificationBean)
                .retrieve()  // レスポンスの取得を開始
                .bodyToMono(Void.class)
                .subscribe(response -> {
                    // レスポンスを処理するコード
                    log.info("通知完了");
                }, error -> {
                    // エラーを処理するコード
                    log.error("Error occurred: " + error.getMessage());
                });
    }
}
