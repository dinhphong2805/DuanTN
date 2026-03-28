package com.kesn.dto;

public class SupportChatResponse {
    private String reply;

    public SupportChatResponse() {
    }

    public SupportChatResponse(String reply) {
        this.reply = reply;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}
