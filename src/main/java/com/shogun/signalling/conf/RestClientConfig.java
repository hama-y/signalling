package com.shogun.signalling.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class RestClientConfig {
    @Value("${notify-service.url}")
    private String notifyServiceUrl;
    @Value("${message-service.url}")
    private String messageServiceUrl;

    @Bean("notifyWebClient")
    public WebClient notifyWebClient() {
        return WebClient.builder().baseUrl(notifyServiceUrl).build();
    }

    @Bean("messageWebClient")
    public WebClient messageWebClient() {
        return WebClient.builder().baseUrl(messageServiceUrl).build();
    }
}
