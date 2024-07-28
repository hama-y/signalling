package com.shogun.signalling;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonChecker {

    public static void sysout(Object obj){
        try {
            // ObjectMapperインスタンスを作成
            ObjectMapper mapper = new ObjectMapper();

            // ReportBeanオブジェクトをJSON文字列に変換
            String json = mapper.writeValueAsString(obj);

            // JSON文字列を標準出力
            System.out.println(json);
        } catch (Exception e) {
            System.out.println("jsonへの変換失敗");
        }
    }
}
