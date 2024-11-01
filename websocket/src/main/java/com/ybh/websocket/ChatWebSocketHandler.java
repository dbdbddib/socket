package com.ybh.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            // getPayload(): 웹소켓에 있는 내용을 가져온다
            String payload = message.getPayload();

            // 직렬된 텍스트를 객체로 만드는 메소드 (ChatMessageDto 필드값이랑 형이 같아야함)
            ChatMessageDto chatMessageDto = objectMapper.readValue(payload, ChatMessageDto.class);
            ChatRoomDto chatRoom = this.chatRoomService.findByRoomId(chatMessageDto.getRoomId());   // roomId 로 방을 찾는다.
            if (chatRoom == null) {
                return;
            }
            ChatWebSocketSession cwss = ChatWebSocketSession.builder()
                    .writer(chatMessageDto.getWriter())
                    .webSocketSession(session).build();
            // 대화명이랑 세션이랑 같이 묶은 객체를 생성
//            List<WebSocketSession> sessionList = byRoomId.getSessionList();
            if (chatMessageDto.getMsgType() == ChatMessageDto.ChatMessageType.ENTER) {
                chatRoom.getChatWebSocketSessions().add(cwss);  // 방의 세션 배열에 추가
                chatMessageDto.setMessage("입장");    // 메세지 가공
                this.sendMessageSessionsInRoom(chatMessageDto, chatRoom.getChatWebSocketSessions());
            } else if (chatMessageDto.getMsgType() == ChatMessageDto.ChatMessageType.OUT) {
                ChatWebSocketSession findChatWebSession = this.findChatWebSocketSession(session, chatRoom.getChatWebSocketSessions());
                chatRoom.getChatWebSocketSessions().remove(findChatWebSession);
                if (chatRoom.getCount() < 1) {
                    // 인원이 0 이면 방을 삭제한다.
                    this.chatRoomService.deleteByRoomId(chatRoom.getRoomId());
                } else {
                    // 인원이 0보다 크면 퇴장 메세지를 전송한다.
                    chatMessageDto.setMessage("퇴장");
                    this.sendMessageSessionsInRoom(chatMessageDto, chatRoom.getChatWebSocketSessions());
                }
            } else if (chatMessageDto.getMsgType() == ChatMessageDto.ChatMessageType.MESSAGE) {
                this.sendMessageSessionsInRoom(chatMessageDto, chatRoom.getChatWebSocketSessions());
            }
        } catch (Exception ex) {
            log.error(ex.toString());
        }
    }

    // 메시지 전송 메소드
    private void sendMessageSessionsInRoom(ChatMessageDto chatMessageDto, List<ChatWebSocketSession> sessionList) throws IOException {
        // 객체 -> String 변환 (JSON 형태)
        String msg = this.objectMapper.writeValueAsString(chatMessageDto);

        // 보낼 메시지 맵핑
        TextMessage tm = new TextMessage(msg);

        for (ChatWebSocketSession chatWebSocketSession : sessionList) {
            try {
                chatWebSocketSession.getWebSocketSession().sendMessage(tm);
            } catch (Exception e) {
                log.error(e.toString());
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.debug("handleTransportError : {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.debug("afterConnectionClosed : {}, {}", session.getId(), status);
        List<ChatRoomDto> all = this.chatRoomService.findAll();
        ChatWebSocketSession findSession = null;
        for (ChatRoomDto chatRoom : all) {
            try {
                Optional<ChatWebSocketSession> find = chatRoom.getChatWebSocketSessions().stream().filter(x -> session.getId().equals(x.getWebSocketSession().getId())).findAny();
                findSession = find.orElse(null);
                if (findSession != null) {
                    chatRoom.getChatWebSocketSessions().remove(findSession);
                    if (chatRoom.getCount() < 1) {
                        // 인원이 0 이면 방을 삭제한다.
                        this.chatRoomService.deleteByRoomId(chatRoom.getRoomId());
                    } else {
                        ChatMessageDto chatMessageDto = ChatMessageDto.builder()
                                .msgType(ChatMessageDto.ChatMessageType.OUT)
                                .writer(findSession.getWriter())
                                .message("퇴장").build();
                        // 인원이 0보다 크면 퇴장 메세지를 전송한다.
                        chatMessageDto.setMessage("퇴장");
                        this.sendMessageSessionsInRoom(chatMessageDto, chatRoom.getChatWebSocketSessions());
                    }
                    break;
                }
            } catch (Exception e) {
                log.error(e.toString());
            }
        }
    }

    private ChatWebSocketSession findChatWebSocketSession(WebSocketSession session
        , List<ChatWebSocketSession> list) {
        for (ChatWebSocketSession chatWebSocketSession : list) {
            if ( session.getId().equals(chatWebSocketSession.getWebSocketSession().getId()) ) {
                return chatWebSocketSession;
            }
        }
        return null;
    }
}
