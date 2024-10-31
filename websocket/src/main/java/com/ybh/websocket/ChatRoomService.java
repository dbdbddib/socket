package com.ybh.websocket;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatRoomService {
    private final Map<String, ChatRoomDto> chatRoomDtoMap = new LinkedHashMap<>();

    public ChatRoomDto insert(String roomName) {
        ChatRoomDto newRoom = ChatRoomDto.builder()
                .roomId(UUID.randomUUID().toString())
                .roomName(roomName).build();
        chatRoomDtoMap.put(newRoom.getRoomId(), newRoom);
        return newRoom;
    }

    public ChatRoomDto findByRoomId(String roomId) {
        return chatRoomDtoMap.get(roomId);
    }

    public List<ChatRoomDto> findAll() {
        return chatRoomDtoMap.values().stream().toList();
    }

    public void deleteByRoomId(String roomId) {
        chatRoomDtoMap.remove(roomId);
    }
}
