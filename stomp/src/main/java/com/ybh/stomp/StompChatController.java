package com.ybh.stomp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

@Slf4j
@Controller
public class StompChatController {
    @Autowired
    private SimpMessageSendingOperations msgTempate;

    @Autowired
    private StompRoomService stompRoomService;

    @MessageMapping("/stomp/message")
    public void message(StompMessageDto stompMessageDto) {
        log.info("/stomp/message => roomId:{}, msgType:{}, writer:{}, message:{}"
                , stompMessageDto.getRoomId()
                , stompMessageDto.getMsgType()
                , stompMessageDto.getWriter()
                , stompMessageDto.getMessage()
        );
        StompRoomDto stompRoom = stompRoomService.findByRoomId(stompMessageDto.getRoomId());
        if (stompRoom == null) {
            return;
        }
        if ( StompMessageDto.StompMessageType.ENTER == stompMessageDto.getMsgType() ) {
            stompRoom.setCount(stompRoom.getCount() + 1);
        } else if ( StompMessageDto.StompMessageType.OUT == stompMessageDto.getMsgType() ) {
            stompRoom.setCount(stompRoom.getCount() - 1);
        }
        if( stompRoom.getCount() < 1 ) {
            stompRoomService.deleteByRoomId(stompMessageDto.getRoomId());
        } else {
            msgTempate.convertAndSend("/sub/stomp/room/" + stompMessageDto.getRoomId()
                    , stompMessageDto);
        }
    }
}
