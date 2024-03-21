package com.goormthon.rememberspring.diary.api.dto.request;

import com.goormthon.rememberspring.diary.api.dto.Message;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatGptRequestDto {
    private String model;
    private List<Message> messages;

    @Builder
    private ChatGptRequestDto(String model, String prompt) {
        this.model = model;
        this.messages =  new ArrayList<>();
        this.messages.add(new Message("user", prompt));
    }
}
