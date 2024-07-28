package com.shogun.signalling.handler;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.shogun.signalling.JsonChecker;
import com.shogun.signalling.bean.CallNotificationBean;
import com.shogun.signalling.enumeration.NotificationType;
import com.shogun.signalling.service.CallingService;
import com.shogun.signalling.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shogun.signalling.model.SignalData;
import com.shogun.signalling.enumeration.SignalType;

@Slf4j
public class SignalingHandler extends TextWebSocketHandler {
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CallingService callingService;

    List<WebSocketSession> sessions = new LinkedList<WebSocketSession>();
    ConcurrentHashMap<Integer, WebSocketSession> sessionMap = new ConcurrentHashMap<>();


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {

        log.info("通信きた");

        SignalData message = objectMapper.readValue(textMessage.getPayload(), SignalData.class);
        Map<String, Object> data = message.getData();
        SignalData response = new SignalData();
        JsonChecker.sysout(message);

        // 電話をかけた時
        if (message.getType().equalsIgnoreCase(SignalType.NEW.toString())) {
            Integer userId = Integer.parseInt(data.get("user_id").toString());
            Integer callerId = Integer.parseInt(data.get("partner_id").toString());

            // 送信者のユーザーIDとsessionの対を保存しておく
            sessionMap.put(userId, session);

            String icon = data.get("user_icon") == null ? null : data.get("user_icon").toString();

            // 通話先相手にpush通知で着信を知らせる
            CallNotificationBean notificationBean = CallNotificationBean.builder()
                    .type(NotificationType.call)
                    .callerId(userId)
                    .callerName(data.get("user_name").toString())
                    .partnerId(callerId)
                    .isVideoCalling((Boolean) data.get("isVideoCalling"))
                    .icon(icon)
                    .needNotify(true)
                    .build();
            notificationService.sendNotification(notificationBean);

            // 非同期で電話をかけはじめ処理開始
            callingService.startCalling(userId, callerId, notificationBean);

            return;
        } // 電話に出た時
        else if (message.getType().equalsIgnoreCase(SignalType.ACCEPT.toString())) {
            Integer userId = Integer.parseInt(data.get("user_id").toString());
            Integer callerId = Integer.parseInt(data.get("partner_id").toString());

            // 送信者のユーザーIDとsessionの対を保存しておく
            sessionMap.put(userId, session);

            boolean isAnswer = callingService.isAnswer(userId, callerId);

            // 受信したら発信者に伝える
            if (isAnswer) {
                response.setType(SignalType.PEERS.toString().toLowerCase());

                if (sessionMap.containsKey(callerId)) {
                    response.setData(Map.of());
                    WebSocketSession callerSession = sessionMap.get(callerId);
                    callerSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                }
            } else {// 既に着信が終わっていいたが、電話に出た時
                response.setType(SignalType.FINISHED.toString().toLowerCase());

                // 受信者にfinished送信
                response.setData(Map.of());
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            }
            return;
        } else if (message.getType().equalsIgnoreCase(SignalType.OFFER.toString())) {
            Integer partnerId = Integer.parseInt(data.get("to").toString());

            session = sessionMap.get(partnerId);
            response.setType(SignalType.OFFER.toString().toLowerCase());
            response.setData(data);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            return;

        } else if (message.getType().equalsIgnoreCase(SignalType.ANSWER.toString())) {
            Integer partnerId = Integer.parseInt(data.get("to").toString());

            session = sessionMap.get(partnerId);

            response.setType(SignalType.ANSWER.toString().toLowerCase());
            response.setData(data);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));

            // 通話開始の処理。実際にはここで開始ではないが。。。
            callingService.connectCalling(partnerId);
            return;
        } else if (message.getType().equalsIgnoreCase(SignalType.BYE.toString())) {
            Integer userId = Integer.parseInt(data.get("user_id").toString());
            Integer partnerId = Integer.parseInt(data.get("partner_id").toString());

            // 電話中リストから削除
            callingService.finishCalling(userId, partnerId);

            if (sessionMap.containsKey(partnerId)) {
                session = sessionMap.get(partnerId);
                response.setType(SignalType.BYE.toString().toLowerCase());
                response.setData(data);
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                sessionMap.remove(partnerId);
            }
            sessionMap.remove(userId);
            return;
        } else if (message.getType().equalsIgnoreCase(SignalType.CANDIDATE.toString().toLowerCase())) {
            Integer partnerId = Integer.parseInt(data.get("to").toString());

            response.setType(SignalType.CANDIDATE.toString().toLowerCase());
            response.setData(data);
            session = sessionMap.get(partnerId);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        } // 電話をかけるのをやめた場合
        else if (message.getType().equalsIgnoreCase(SignalType.QUIT.toString())) {
            Integer userId = Integer.parseInt(data.get("user_id").toString());
            Integer partnerId = Integer.parseInt(data.get("partner_id").toString());

            // 通話先相手に通話キャンセル用のpush通知
            CallNotificationBean notificationBean = CallNotificationBean.builder()
                    .type(NotificationType.callquit)
                    .callerId(userId)
                    .callerName(
                            data.get("user_name").toString())
                    .partnerId(partnerId)
                    .needNotify(false)
                    .build();
            notificationService.sendNotification(notificationBean);

            // 通話がキャンセルになったことのメッセージ登録
            callingService.quitCalling(userId, partnerId);
            return;
        } else if (message.getType().equalsIgnoreCase(SignalType.CONNECT.toString())) {
            Integer userId = Integer.parseInt(data.get("user_id").toString());
            sessionMap.put(userId, session);
            return;
        } else if (message.getType().equalsIgnoreCase(SignalType.MISSED.toString())) {
            Integer callerId = Integer.parseInt(data.get("user_id").toString());
            Integer partnerId = Integer.parseInt(data.get("partner_id").toString());

            // 電話中リストから削除
            callingService.missedCall(callerId, partnerId);
            sessionMap.remove(callerId);
            return;
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        sessions.add(session);
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

        sessions.remove(session);
        super.afterConnectionClosed(session, status);
    }
}
