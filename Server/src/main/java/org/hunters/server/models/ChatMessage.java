package org.hunters.server.models;


public class ChatMessage {
    public int chatId;
    public String msg;

    public ChatMessage(int chatId, String msg) {
        this.chatId = chatId;
        this.msg    = msg;
    }
}