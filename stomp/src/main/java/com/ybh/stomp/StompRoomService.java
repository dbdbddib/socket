package com.ybh.stomp;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class StompRoomService {
    private final Map<String, StompRoomDto> stompRoomDtoMap = new LinkedHashMap<>();

    public StompRoomDto insert(String roomName) {
        StompRoomDto newRoom = StompRoomDto.builder()
                .roomId(UUID.randomUUID().toString())
                .roomName(roomName)
                .count(0)
                .build();
        stompRoomDtoMap.put(newRoom.getRoomId(), newRoom);
        return newRoom;
    }

    public StompRoomDto findByRoomId(String roomId) {
        return stompRoomDtoMap.get(roomId);
    }

    public List<StompRoomDto> findAll() {
        return stompRoomDtoMap.values().stream().toList();
    }

    public void deleteByRoomId(String roomId) {
        stompRoomDtoMap.remove(roomId);
    }
}
