package com.shogun.signalling.service;

import com.shogun.signalling.bean.CallNotificationBean;
import com.shogun.signalling.bean.CustomMessageBean;
import com.shogun.signalling.enumeration.CallingMessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class CallingService {
    private final MessageService messageService;
    // 通話中のMap key:電話をかけたUserId value:電話をかけられたUserId
    ConcurrentHashMap<Integer, Integer> callingMap = new ConcurrentHashMap<>();
    // 電話をかけて相手がまだ出ていないMap key:電話をかけたUserId value:電話をかけられたUserId
    ConcurrentHashMap<Integer, Integer> ringingMap = new ConcurrentHashMap<>();
    // 電話をかけたユーザーの情報を保持し、通話終了タイプによってメッセージに履歴を残す key:電話をかけたUserId value:そのユーザーの情報など
    ConcurrentHashMap<Integer, CallNotificationBean> userInfoMap = new ConcurrentHashMap<>();
    // 通話開始時間を保持　key:電話をかけたUserId value:開始時間のエポックタイムからのミリ秒
    ConcurrentHashMap<Integer, Long> callTimestampMap = new ConcurrentHashMap<>();

    @Value("${calling.duration}")
    private int callingDuration;

    /**
     * 通話をかけだした時の処理
     *
     * @param callerId
     * @param partnerId
     * @param notificationBean
     * @throws InterruptedException
     */
    @Async
    public void startCalling(Integer callerId, Integer partnerId, CallNotificationBean notificationBean) throws InterruptedException {
        ringingMap.put(callerId, partnerId);
        userInfoMap.put(callerId, notificationBean);
    }

    public void missedCall(Integer callerId, Integer partnerId){
        // まだ電話に出ていなければ削除する
        if (ringingMap.containsKey(callerId) && ringingMap.get(callerId).equals(partnerId)) {
            log.info(callerId + "が" + partnerId + "にかけた通話は、通話に出なかったため終了");
            ringingMap.remove(callerId);

            // 通話が不在着信であることのメッセージ登録
            sendMessage(callerId, CallingMessageType.MISSED_CALL, true);
        }
    }


    /**
     * 着信に出た時にまだ相手が通話をかけているかチェック
     *
     * @param userId
     * @param callerId
     * @return
     */
    public boolean isAnswer(Integer userId, Integer callerId) {
        if (ringingMap.containsKey(callerId) && ringingMap.get(callerId).equals(userId)) {
            ringingMap.remove(callerId);
            callingMap.put(callerId, userId);
            log.info(userId + "と" + callerId + "が通話開始");
            return true;
        } else {// 電話かけた人がかけるのをやめたタイミングで出た時はこっち
            return false;
        }
    }


    /**
     * 通話が開始した時の処理
     *
     * @param callerId
     */
    @Async
    public void connectCalling(Integer callerId) {
        // 開始時間を保存
        callTimestampMap.put(callerId, System.currentTimeMillis());
    }


    /**
     * 電話をやめたときに使用
     * 誰が通話を切ったかわからないので、どちらでも対応できるように
     *
     * @param userId
     * @param userId2
     */
    @Async
    public void finishCalling(Integer userId, Integer userId2) {
        log.info(userId + "と" + userId2 + "が通話終了");

        if (callingMap.containsKey(userId) && callingMap.get(userId).equals(userId2)) {
            callingMap.remove(userId);
            sendMessage(userId, CallingMessageType.FINISH, false);
        } else if (callingMap.containsKey(userId2) && callingMap.get(userId2).equals(userId)) {
            callingMap.remove(userId2);
            sendMessage(userId2, CallingMessageType.FINISH, false);
        } // 通話にまだてていない場合は、ringingMapに値がある。着信の拒否
        else if (ringingMap.containsKey(userId2) && ringingMap.get(userId2).equals(userId)) {
            ringingMap.remove(userId2);

            // 通話キャンセルのメッセージ登録
            sendMessage(userId2, CallingMessageType.CANCEL, false);
        }
    }


    /**
     * 着信を拒否した時の処理
     *
     * @param callerId
     * @param userId
     */
    @Async
    public void quitCalling(Integer callerId, Integer userId) {
        log.info(userId + "が" + callerId + "の通話を拒否");

        // 通話にまだてていない場合は、ringingMapに値がある。着信のキャンセル
        if (ringingMap.containsKey(callerId) && ringingMap.get(callerId).equals(userId)) {
            ringingMap.remove(callerId);

            // 通話が不在着信であることのメッセージ登録
            sendMessage(callerId, CallingMessageType.CANCEL, true);
        }
    }


    private void sendMessage(Integer callerId, CallingMessageType callingMessageType, boolean needNotify) {
        Long duration = null;
        if (callTimestampMap.containsKey(callerId)) {
            duration = System.currentTimeMillis() - callTimestampMap.get(callerId);
            callTimestampMap.remove(callerId);
        }
        CustomMessageBean customMessageBean = CustomMessageBean.create(userInfoMap.get(callerId), System.currentTimeMillis(), callingMessageType, duration,needNotify);
        userInfoMap.remove(callerId);
        messageService.sendMessage(customMessageBean);
    }
}
