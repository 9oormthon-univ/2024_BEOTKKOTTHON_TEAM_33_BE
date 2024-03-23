package com.goormthon.rememberspring.diary.application;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goormthon.rememberspring.diary.api.dto.request.ChatGptRequestDto;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryGeneratorService {

    @Value("${spring.openai.model}")
    private String model;

    @Value("${spring.openai.api.url}")
    private String apiURL;

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

        // 리스트의 첫 번째 값(이미지)가 대표 이미지 이므로, 리스트의 첫 번째 값을 보냄.
        // 프롬프트 엔지니어링 -> 감정, 일기 타입, 음성 텍스트, 이미지를 보내, 응답값 받아옴
        ChatGptRequestDto request = ChatGptRequestDto.builder()
                .model(model)
                .prompt(buildQuery(imageResDto.get(0), diaryContentRequestDto))
                .build();

        ChatGptResponseDto chatGptResponseDto = template.postForObject(apiURL, request, ChatGptResponseDto.class);

        DiaryContentResponseDto diaryContentResponseDto;
        try {
            diaryContentResponseDto = objectMapper.readValue(parseJson(chatGptResponseDto), DiaryContentResponseDto.class);
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

        ChatGptRequestDto request = ChatGptRequestDto.builder()
                .model(model)
                .prompt(buildQuery(imageResDto.get(0), requestDto))
                .build();

        ChatGptResponseDto chatGptResponseDto = template.postForObject(apiURL, request, ChatGptResponseDto.class);

        DiaryContentResponseDto diaryContentResponseDto;
        try {
            diaryContentResponseDto = objectMapper.readValue(parseJson(chatGptResponseDto), DiaryContentResponseDto.class);
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

    private String buildQuery(ImageResDto imageFile, DiaryContentRequestDto dto) throws Exception {

        return dto.getVoiceText() != null ?
                "\n{\ntype :  text, text : "
                        + "\n일기 타입 : " + dto.getDiaryType()
                        + "\n내가 느낀 감정 : " + dto.getEmotion()
                        + "\n음성 텍스트 : " + dto.getVoiceText()
                        + "\n질문 : 일기타입, 내가 느낀 감정, 음성텍스트, 이미지를 바탕으로 일기를 구체적으로 작성해줘."
                        + "\n반환 형식 : "
                        + "\ntitle : %s"
                        + "\ndate : " + new SimpleDateFormat("yyyy년 MM월 dd일").format(new Date())
                        + "\nhashtag : [String Array]"
                        + "\ncontents : %s"
                            + "\n"
                        + "\ntitle은 일기 제목이며 10자로 제한한다. 자연스러운 일기제목이여야 한다."
                        + "\nhashtag 일기 내용과 관련 있는 해시태그이며, 2~4개의 해시태그를 생성하고, 하나의 해시태그는 5자로 제한한다."
                        + "해시태그 단어 앞에는 반드시 #을 붙인다."
                        + "\ncontents는 일기 내용이며, 350자로 제한한다. 일기 내용은 반드시 반말로 반환해야만 한다."
                        + "\ndate는 정해진 형식을 반환하면 된다."
                        + "\ntitle, date, hashTag, contents를 Text JSON로 변경해서 반환한다."
                        + "\n}"
                        + "\n{\ntype: image_url, image_url: {\n "+
            "url :" + imageFile.convertImageUrl() +"\n}"
                :
                "\n{\ntype :  text, text : "
                        + "\ndate : " + new SimpleDateFormat("yyyy년 MM월 dd일").format(new Date())
                +       "\n일기 타입 : " + dto.getDiaryType()
                +       "\n내가 느낀 감정 : " + dto.getEmotion()
                +       "\n질문 : 일기타입, 내가 느낀 감정, 이미지를 바탕으로 일기를 구체적으로 작성해줘.}"
                + "\n{\ntype: image_url, image_url: {\n "+
                        "url :" + imageFile.convertImageUrl() +"\n}"
                ;

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
