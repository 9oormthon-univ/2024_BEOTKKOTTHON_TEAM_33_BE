package com.goormthon.rememberspring.diary.application;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goormthon.rememberspring.diary.api.dto.request.DiaryContentRequestDto;
import com.goormthon.rememberspring.diary.api.dto.request.DiaryRetryRequestDto;
import com.goormthon.rememberspring.diary.api.dto.response.ChatGptResponseDto;
import com.goormthon.rememberspring.diary.api.dto.response.DiaryContentResponseDto;
import com.goormthon.rememberspring.diary.api.dto.response.DiaryGeneratorResponseDto;
import com.goormthon.rememberspring.diary.domain.entity.Diary;
import com.goormthon.rememberspring.diary.domain.entity.Hashtag;
import com.goormthon.rememberspring.diary.domain.repository.DiaryHashtagRepository;
import com.goormthon.rememberspring.diary.domain.repository.DiaryRepository;
import com.goormthon.rememberspring.diary.domain.repository.HashtagRepository;
import com.goormthon.rememberspring.diary.excepion.DiaryNotFoundException;
import com.goormthon.rememberspring.image.api.dto.response.ImageResDto;
import com.goormthon.rememberspring.image.domain.Image;
import com.goormthon.rememberspring.image.domain.repository.ImageRepository;
import com.goormthon.rememberspring.member.domain.Member;
import com.goormthon.rememberspring.member.domain.repository.MemberRepository;
import com.goormthon.rememberspring.member.exception.MemberNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DiaryGeneratorService {

    @Value("${spring.openai.model}")
    private String model;

    @Value("${spring.openai.api.url}")
    private String apiURL;

    @Value("${spring.openai.api.key}")
    private String apiKey;

    @Autowired
    private RestTemplate template;
    private final ObjectMapper objectMapper;
    private final ImageRepository imageRepository;
    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final HashtagRepository hashtagRepository;
    private final DiaryHashtagRepository diaryHashtagRepository;

    @Transactional
    public DiaryGeneratorResponseDto chat(String email, DiaryContentRequestDto diaryContentRequestDto) throws Exception {
        // 인증 시, 헤더에 실려온 토큰을 분석하여 이메일을 받아와, Member 객체 받아옴.
        Member member = memberRepository.findByEmail(email).orElse(null);
        // 아직 일기 생성이 안되었으므로, 이미지 DB의 diary_id 컬럼은  null
        List<Image> getImages = imageRepository.findByDiaryAndMember(null, member);

        List<ImageResDto> imageResDto = getImageResDtos(getImages);

        DiaryContentResponseDto diaryContentResponseDto;
        try {
            diaryContentResponseDto = objectMapper.readValue(parseJson(buildQuery(imageResDto.get(0), diaryContentRequestDto)), DiaryContentResponseDto.class);
        } catch (JsonParseException e) {
            throw new RuntimeException(e);
        }

        Diary diary = Diary.toEntity(
                diaryContentResponseDto,
                diaryContentRequestDto,
                getImages,
                member
        );

        diaryRepository.save(diary);

        // 해시태그 저장
        for (String tagName : diaryContentResponseDto.getHashtag()) {
            Hashtag hashtag = hashtagRepository.findByName(tagName)
                    .orElseGet(() ->
                            hashtagRepository.save(Hashtag.builder()
                                .name(tagName)
                                .build()));

            diary.addHashtagMapping(hashtag);
        }

        for (Image images : getImages) {
            images.updateImage(diary);
        }

        return DiaryGeneratorResponseDto.from(diary, imageResDto);
    }

    @Transactional
    public DiaryGeneratorResponseDto retry(String email, DiaryRetryRequestDto diaryRetryRequestDto) throws Exception {
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        Diary diary = diaryRepository.findById(diaryRetryRequestDto.getDiaryId()).orElseThrow(DiaryNotFoundException::new);
        List<Image> getImages = imageRepository.findByDiaryAndMember(diary, member);

        DiaryContentRequestDto requestDto = new DiaryContentRequestDto(
                diary.getDiaryType(),
                diary.getEmotion(),
                diaryRetryRequestDto.getVoiceText()
        );

        List<ImageResDto> imageResDto = getImageResDtos(getImages);

        DiaryContentResponseDto diaryContentResponseDto;
        try {
            diaryContentResponseDto = objectMapper.readValue(parseJson(buildQuery(imageResDto.get(0), requestDto)), DiaryContentResponseDto.class);
        } catch (JsonParseException e) {
            throw new RuntimeException(e);
        }

        diary.updateDiary(diaryContentResponseDto.getContents(), diaryRetryRequestDto.getVoiceText());

        // 해시태그 저장
        for (String tagName : diaryContentResponseDto.getHashtag()) {
            Hashtag hashtag = hashtagRepository.findByName(tagName)
                    .orElseGet(() ->
                            hashtagRepository.save(Hashtag.builder()
                                    .name(tagName)
                                    .build()));

            if (!diaryHashtagRepository.existsByDiaryAndHashtag(diary, hashtag)) {
                diary.addHashtagMapping(hashtag);
            }
        }

        return DiaryGeneratorResponseDto.from(diary, imageResDto);
    }

    private ChatGptResponseDto buildQuery(ImageResDto imageFile, DiaryContentRequestDto dto) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        String prompt = dto.getVoiceText() != null ? "{"
                + "\"model\": \"" + model + "\", "
                + "\"messages\": ["
                +     "{"
                +         "\"role\": \"user\","
                +         "\"content\": ["
                +             "{"
                +                 "\"type\": \"text\","
                +                 "\"text\": \"일기타입은 " +  dto.getDiaryType()
                +                             "내 감정 상태는 " + dto.getEmotion()
                +                             "음성텍스트는 " + dto.getVoiceText() + " 이 3가지로 일기를 작성해줘."
                +                             "반환은 title(String), hashtag(List), contents(String)이고 정확하게 JSON 으로 반환해줘."
                +                             "title은 10자로 제한하고, 이미지와 잘 어울리는 것으로 만들어줘."
                +                             "contents는 350자로 제한할게."
                +                             "hashtag를 반환할 때는 앞에 #을 꼭 붙여주고, 4개 이하의 해시태그를 생성해줘. 해시태그는 4자로 제한할게."
                +                             "너의 반환값인 content 안에 JSON 반환 이외에 말은 안해도돼.\""
                +             "},"
                +             "{"
                +                 "\"type\": \"image_url\","
                +                 "\"image_url\": {"
                +                     "\"url\": \"" + imageFile.convertImageUrl() + "\""
                +                 "}"
                +             "}"
                +         "]"
                +     "}"
                + "]"
                + "}" : "{"
                + "\"model\": \"" + model + "\", "
                + "\"messages\": ["
                +     "{"
                +         "\"role\": \"user\","
                +         "\"content\": ["
                +             "{"
                +                 "\"type\": \"text\","
                +                 "\"text\": \"일기타입은 " +  dto.getDiaryType()
                +                             "내 감정 상태는 " + dto.getEmotion()
                +                             "이 2가지로 일기를 작성해줘."
                +                             "반환은 title(String), hashtag(List), contents(String)이고 정확하게 JSON 으로 반환해줘."
                +                             "title은 10자로 제한하고, 이미지와 잘 어울리는 것으로 만들어줘."
                +                             "contents는 350자로 제한할게."
                +                             "hashtag를 반환할 때는 앞에 #을 꼭 붙여주고, 4개 이하의 해시태그를 생성해줘. 해시태그는 4자로 제한할게."
                +                             "너의 반환값인 content 안에 JSON 반환 이외에 말은 안해도돼.\""
                +             "},"
                +             "{"
                +                 "\"type\": \"image_url\","
                +                 "\"image_url\": {"
                +                     "\"url\": \"" + imageFile.convertImageUrl() + "\""
                +                 "}"
                +             "}"
                +         "]"
                +     "}"
                + "]"
                + "}";

        HttpRequest request = null;

        try {
            request = HttpRequest.newBuilder()
                    .uri(URI.create(apiURL))
                    .header("Content-Type", "application/json")
                    .header("Authorization",  String.format("Bearer %s", apiKey))
                    .POST(BodyPublishers.ofString(prompt))
                    .build();
        } catch (RuntimeException e) {
            request = HttpRequest.newBuilder()
                    .uri(URI.create(apiURL))
                    .header("Content-Type", "application/json")
                    .header("Authorization",  String.format("Bearer %s", apiKey))
                    .POST(BodyPublishers.ofString(prompt))
                    .build();
        }

        // HttpRequest를 전송하고 HttpResponse를 받음
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        log.info(response.body());
        ChatGptResponseDto chatGptResponseDto;
        try {
            chatGptResponseDto = objectMapper.readValue(response.body(), ChatGptResponseDto.class);
        } catch (JsonParseException e) {
            throw new RuntimeException(e);
        }

        return chatGptResponseDto;
    }

    private List<ImageResDto> getImageResDtos(List<Image> getImages) {
        List<ImageResDto> imageResDto = new ArrayList<>();

        for (Image image : getImages) {
            imageResDto.add(ImageResDto.from(image));
        }
        return imageResDto;
    }

    private String parseJson(ChatGptResponseDto chatGptResponseDto) {
        String content = chatGptResponseDto.getChoices().get(0).getMessage().getContent();
        content = content.substring(7);
        content = content.substring(0, content.length() - 4);

        return content;
    }
}
