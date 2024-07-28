package com.shogun.signalling.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shogun.signalling.JsonChecker;
import com.shogun.signalling.bean.CallNotificationBean;
import com.shogun.signalling.bean.CustomMessageBean;
import com.shogun.signalling.bean.NotificationBean;
import com.shogun.signalling.enumeration.CallingMessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MessageService {
    @Autowired
    @Qualifier("messageWebClient")
    private WebClient webClient;


    public void sendMessage(CustomMessageBean customMessageBean) {
        JsonChecker.sysout(customMessageBean);

        // メッセージ基盤にカスタムメッセージを登録してもらう
        webClient.post()
                .uri("/message/save")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(customMessageBean)
                .retrieve()  // レスポンスの取得を開始
                .bodyToMono(Void.class)
                .subscribe(response -> {
                    // レスポンスを処理するコード
                    log.info("カスタムメッセージ登録完了");
                }, error -> {
                    // エラーを処理するコード
                    log.error("Error occurred: " + error.getMessage());
                });
    }
}
