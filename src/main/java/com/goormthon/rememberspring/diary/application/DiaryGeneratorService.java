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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DiaryGeneratorService {

    @Value("${spring.openai.api.url}")
    private String apiURL;

    @Value("${spring.openai.api.key}")
    private String apiKey;

    @Value("${prompt.withVoiceText}")
    private String withVoiceText;

    @Value("${prompt.withoutVoiceText}")
    private String withoutVoiceText;

    private final ObjectMapper objectMapper;
    private final ImageRepository imageRepository;
    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final HashtagRepository hashtagRepository;
    private final DiaryHashtagRepository diaryHashtagRepository;

    @Transactional
    public DiaryGeneratorResponseDto chat(String email, DiaryContentRequestDto diaryContentRequestDto) throws Exception {
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
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

        for (String tagName : diaryContentResponseDto.getHashtags()) {
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

        for (String tagName : diaryContentResponseDto.getHashtags()) {
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
        String template = dto.getVoiceText() != null ? withVoiceText: withoutVoiceText;

        String prompt = template.replace("<diaryType>", dto.getDiaryType().toString())
                .replace("<motion>", dto.getEmotion().toString())
                .replace("<voiceText>", dto.getVoiceText() != null ? dto.getVoiceText() : "")
                .replace("<imageUrl>", imageFile.convertImageUrl());

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
        log.info("<파싱 시작!> "+ String.valueOf(chatGptResponseDto.getChoices().get(0)));
        String content = chatGptResponseDto.getChoices().get(0).getMessage().getContent();
        content = content.substring(7);
        content = content.substring(0, content.length() - 4);

        log.info("<파싱 끝!> " + content);

        return content;
    }
}
