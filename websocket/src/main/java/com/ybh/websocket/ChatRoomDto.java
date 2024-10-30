package com.ybh.websocket;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder

public class ChatRoomDto {
    private String roomName;
    private String roomId;
}
