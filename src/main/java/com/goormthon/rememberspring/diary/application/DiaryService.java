package com.goormthon.rememberspring.diary.application;

import com.goormthon.rememberspring.diary.api.dto.request.ChatGptRequestDto;
import com.goormthon.rememberspring.diary.api.dto.request.DiaryContentRequestDto;
import com.goormthon.rememberspring.diary.api.dto.response.ChatGptResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiaryService {

    @Value("${spring.openai.model}")
    private String model;

    @Value("${spring.openai.api.url}")
    private String apiURL;

    @Autowired
    private RestTemplate template;

    public ChatGptResponseDto chat(List<MultipartFile> imageFile, DiaryContentRequestDto dto) throws Exception{
        ChatGptRequestDto request = new ChatGptRequestDto(model, buildQuery(imageFile, dto));
        System.out.println(buildQuery(imageFile, dto));
        return template.postForObject(apiURL, request, ChatGptResponseDto.class);
    }

    private String buildQuery(List<MultipartFile> imageFile, DiaryContentRequestDto dto) throws Exception{
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("당신이 기록하고 싶은 일기의 타입: ").append(dto.getDiaryType())
                .append("\n오늘 느낀 감정: ").append(dto.getEmotion())
                .append("\n음성 텍스트: ").append(dto.getVoiceText())
                .append("\n이미지:\n");

        // Append details of each image file
        for (MultipartFile file : imageFile) {
            queryBuilder.append("  파일 이름: ").append(file.getOriginalFilename());
        }

        queryBuilder.append("일기의 타입, 감정, 음성 텍스트, 이미지를 구체적으로 활용하고\n")
                .append("이미지에 있는 것들을 종합적으로 분석해서\n")
                .append("오늘의 일기에 들어갈 텍스트를 %s 형식으로 작성해서 반환해줘.\n")
                .append("단, 문장이 끝날 때 마다 줄 바꿈을 자동으로 해서 반환해줬음 좋겠어.");

        return queryBuilder.toString();
    }
}
