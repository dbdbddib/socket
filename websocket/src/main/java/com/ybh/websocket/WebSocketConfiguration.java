package com.ybh.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor

// 웹소켓 구성 ( 웹소켓 초기설정 )
public class WebSocketConfiguration implements WebSocketConfigurer {
    private final WebSocketHandler webSocketHandler;

    // WebSocket 핸들러 등록 부분
    // 매개변수: (WebSocketHandlerRegistry:  WebSocket 경로와 핸들러를 설정)
    // .setAllowedOrigins("*"): "*"로 설정하면 모든 출처에서의 WebSocket 요청을 허용
    // .setAllowedOriginPatterns("*"): Spring 5.3부터 추가된 메서드로, 더 유연한 출처 설정을 위해 사용
    // /ws/chat 경로로 들어오는 WebSocket 연결 요청을 처리하는 메소드
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws/chat")
                .setAllowedOrigins("*")
                .setAllowedOriginPatterns("*");
    }
}
