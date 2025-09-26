package com.necsws.websocketrpapoc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	@Value("${spring.mvc.cors.allowed-origins}")
    private String allowedOrigins;
	
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    	
        registry.addHandler(simpleHandler(), "/wsDwpHandler")
        .setAllowedOrigins(allowedOrigins != null ? allowedOrigins.split(",") : new String[]{});
        
//      registry.addHandler(simpleHandler(), "/simpleHandler").setAllowedOrigins("*");
    }

    @Bean
    public WebSocketHandler simpleHandler() {
        return new SimpleHandler();
    }

}
