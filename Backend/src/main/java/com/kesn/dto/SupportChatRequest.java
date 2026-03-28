package com.kesn.dto;

import java.util.List;

public class SupportChatRequest {
    private List<ChatTurnDto> messages;

    public List<ChatTurnDto> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatTurnDto> messages) {
        this.messages = messages;
    }
}
