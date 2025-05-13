package com.springboot.EventApp.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private GroupWebSocketHandler groupHandler;

    @Autowired
    private PollWebSocketHandler pollHandler;

    @Autowired
    private EventChatWebSocketHandler eventChatWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(groupHandler, "/ws/{groupId}").setAllowedOrigins("*");
        registry.addHandler(pollHandler, "/ws/poll/{pollId}").setAllowedOrigins("*");
        registry.addHandler(eventChatWebSocketHandler, "ws/chat/{groupId}").setAllowedOrigins("*");
    }
}