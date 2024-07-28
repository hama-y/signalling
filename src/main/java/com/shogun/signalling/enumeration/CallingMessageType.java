package com.shogun.signalling.enumeration;

import lombok.Getter;

public enum CallingMessageType {
    MISSED_CALL("不在着信"),
    CANCEL("キャンセル"),
    FINISH("通話終了"),
    ;


    @Getter
    private final String value;
    CallingMessageType(String value) {
        this.value=value;
    }
}
