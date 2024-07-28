package com.shogun.signalling.conf;

import com.shogun.signalling.handler.SignalingHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class SignalingConfiguration implements WebSocketConfigurer{

	@Value( "${allowed.origin:*}" )
	private String allowedOrigin;
	
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

		registry.addHandler(signalingHandler(), "/signalling").setAllowedOrigins(allowedOrigin);
	}
	@Bean
	public SignalingHandler signalingHandler() {
		return new SignalingHandler();
	}
}
