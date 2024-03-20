package com.goormthon.rememberspring.diary.api.dto.response;

import com.goormthon.rememberspring.diary.api.dto.Message;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatGptResponseDto {

    private List<Choice> choices;
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {
        private int index;
        private Message message;
    }
}
