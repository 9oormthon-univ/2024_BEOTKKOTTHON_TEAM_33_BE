package com.goormthon.rememberspring.diary.application;

import com.goormthon.rememberspring.diary.api.dto.request.ChatGptRequestDto;
import com.goormthon.rememberspring.diary.api.dto.request.DiaryContentRequestDto;
import com.goormthon.rememberspring.diary.api.dto.response.ChatGptResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DiaryService {

    @Value("${spring.openai.model}")
    private String model;

    @Value("${spring.openai.api.url}")
    private String apiURL;

    @Autowired
    private RestTemplate template;

    public ChatGptResponseDto chat(MultipartFile imageFile, DiaryContentRequestDto dto){
        ChatGptRequestDto request = new ChatGptRequestDto(model, buildQuery(dto));
        return template.postForObject(apiURL, request, ChatGptResponseDto.class);
    }

    private String buildQuery(DiaryContentRequestDto dto) {
        return String.format(
                "당신이 기록하고 싶은 일기의 타입 : %s\n" +
                        "오늘 느낀 감정 : %s\n" +
                        "음성 텍스트 : %s\n" +
                        "일기의 타입, 감정, 음성 텍스트를 활용하여 오늘의 일기에 들어갈 텍스트를 " +
                        "%%s 형식으로 구체적으로 작성해서 반환해줘.",
                dto.getDiaryType(), dto.getEmotion(), dto.getVoiceText()
        );
    }
}
