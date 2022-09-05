package com.catiger.rtpconsumer.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry)
    {
        // webSocket通道
        // 指定处理器和路径
        registry.addHandler(new SpringWebSocketHandler(), "/ws/websocket")
                .addInterceptors(new WebSocketInterceptor())
                .setAllowedOrigins("*");
        // sockJs通道
        registry.addHandler(new SpringWebSocketHandler(), "/ws/sock-js")
                .addInterceptors(new WebSocketInterceptor())
                .setAllowedOrigins("*")
                .withSockJS();
    }

    @Bean
    public TextWebSocketHandler webSocketHandler()
    {
        return new SpringWebSocketHandler();
    }

}
