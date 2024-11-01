package com.ybh.websocket;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Controller
@RequestMapping("/chat")
public class ChatRoomController {
    @Autowired
    private ChatRoomService chatRoomService;

    @GetMapping("/list")
    public String chatRoomList(Model model) {
        model.addAttribute("chatRoomList", this.chatRoomService.findAll());
        return "chat/chatroomlist";
    }

    @PostMapping("/create")
    public String createChatRoom(Model model, String roomName, String writer) {
        if (roomName != null && !roomName.isEmpty() && writer != null && !writer.isEmpty()) {
            ChatRoomDto newRoom = this.chatRoomService.insert(roomName);
            String url = "redirect:/chat/enter?roomId=" + newRoom.getRoomId() + "&writer=" + URLEncoder.encode(writer, StandardCharsets.UTF_8);
            return url;
//        } else if (roomName != null && !roomName.isEmpty()) {
//            this.chatRoomService.insert(roomName);
        }
        return "redirect:/chat/list";
    }

    @GetMapping("/enter")   // GET ? 와 &
//    @GetMapping("/enter/{roomId}/{writer}") // GET 주소로 데이터 전달
    public String enterChatRoom(Model model
            , HttpServletRequest request
            , @RequestParam String roomId   // GET ? 와 &
//            , @PathVariable String roomId   // GET 주소
            , @RequestParam String writer   // GET ? 와 &
//            , @PathVariable String writer   // GET 주소
    ) {
        model.addAttribute("chatRoomDto", this.chatRoomService.findByRoomId(roomId));
        model.addAttribute("writer", writer);
        String url = String.format("%s:%d", request.getServerName(), request.getServerPort());
        model.addAttribute("hostUrl", url);
        return "chat/chatroomdetail";
    }
}
