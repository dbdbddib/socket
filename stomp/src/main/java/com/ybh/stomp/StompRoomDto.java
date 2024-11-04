package com.ybh.stomp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StompRoomDto {
    private String roomName;
    private String roomId;
    private final List<StompWebSocketSession> stompWebSocketSessions = new ArrayList<>();

    public Integer getCount() {
        return stompWebSocketSessions.size();
    }
}
