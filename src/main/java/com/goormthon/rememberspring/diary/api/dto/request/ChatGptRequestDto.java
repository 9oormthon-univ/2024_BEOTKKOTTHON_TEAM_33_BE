package com.goormthon.rememberspring.diary.api.dto.request;

import com.goormthon.rememberspring.diary.api.dto.Message;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChatGptRequestDto {
    private String model;
    private List<Message> messages;

    public ChatGptRequestDto(String model, String prompt) {
        this.model = model;
        this.messages =  new ArrayList<>();
        this.messages.add(new Message("user", prompt));
    }
}
