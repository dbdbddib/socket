package com.ybh.stomp;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.web.socket.WebSocketSession;

@Getter
@Setter
@SuperBuilder
public class StompWebSocketSession {
    private WebSocketSession webSocketSession;
    private String writer;
}
