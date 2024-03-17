package com.goormthon.rememberspring.diary.application;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goormthon.rememberspring.diary.api.dto.request.ChatGptRequestDto;
import com.goormthon.rememberspring.diary.api.dto.request.DiaryContentRequestDto;
import com.goormthon.rememberspring.diary.api.dto.response.ChatGptResponseDto;
import com.goormthon.rememberspring.diary.api.dto.response.DiaryContentResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.Date;


@Service
public class DiaryService {

    @Value("${spring.openai.model}")
    private String model;

    @Value("${spring.openai.api.url}")
    private String apiURL;

    @Autowired
    private RestTemplate template;
    private final ObjectMapper objectMapper;

    public DiaryService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public DiaryContentResponseDto chat(MultipartFile imageFile, DiaryContentRequestDto dto) throws Exception{
        ChatGptRequestDto request = new ChatGptRequestDto(model, buildQuery(imageFile, dto));

        System.out.println(request);
        ChatGptResponseDto chatGptResponseDto = template.postForObject(apiURL, request, ChatGptResponseDto.class);

        // 백틱이 포함된 컨텐츠
        String content = chatGptResponseDto.getChoices().get(0).getMessage().getContent();
        content = content.substring(7);
        content = content.substring(0, content.length()-4);

        try {
            return objectMapper.readValue(content, DiaryContentResponseDto.class);
        } catch (JsonParseException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildQuery(MultipartFile imageFile, DiaryContentRequestDto dto) throws Exception {

        return dto.getVoiceText() != null ?
                "\n{\ntype :  text, text : "
                +       "\n일기 타입 : " + dto.getDiaryType()
                +       "\n내가 느낀 감정 : " + dto.getEmotion()
                +       "\n음성 텍스트 : " + dto.getVoiceText()
                        + "\n질문 : 일기타입, 내가 느낀 감정, 음성텍스트, 이미지를 바탕으로 일기를 구체적으로 작성해줘."
                        + "\n반환 형식 : "
                        + "\ntitle : %s"
                        + "\ndate : " + new SimpleDateFormat("yyyy년 MM월 dd일").format(new Date())
                        + "\nhashTag : [String Array]"
                        + "\ncontents : %s"
                            + "\n"
                        + "\ntitle은 일기 제목이며 10자로 제한한다."
                        + "\nhashTag는 일기 내용과 관련 있는 해시태그이며, 2~4개의 해시태그를 생성하고, 하나의 해시태그는 5자로 제한한다."
                        + "\ncontents는 일기 내용이며, 350자로 제한한다."
                        + "\ndate는 정해진 형식을 반환하면 된다."
                        + "\ntitle, date, hashTag, contents를 Text JSON로 변경해서 반환한다."
                        + "\n}"
                        + "\n{\ntype: image_url, image_url: {\n "+
            "url : https://i.pinimg.com/474x/8b/f3/a4/8bf3a4600ff3c92650605434da3c1b5c.jpg\n}"
                :
                "\n{\ntype :  text, text : "
                        + "\ndate : " + new SimpleDateFormat("yyyy년 MM월 dd일").format(new Date())
                +       "\n일기 타입 : " + dto.getDiaryType()
                +       "\n내가 느낀 감정 : " + dto.getEmotion()
                +       "\n질문 : 일기타입, 내가 느낀 감정, 이미지를 바탕으로 일기를 구체적으로 작성해줘.}"
                + "\n{\ntype: image_url, image_url: {\n "+
                "url : https://i.pinimg.com/474x/8b/f3/a4/8bf3a4600ff3c92650605434da3c1b5c.jpg\n}"
                ;

    }
}
