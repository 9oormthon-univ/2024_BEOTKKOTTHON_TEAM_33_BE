package com.goormthon.rememberspring.diary.application;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goormthon.rememberspring.diary.api.dto.request.ChatGptRequestDto;
import com.goormthon.rememberspring.diary.api.dto.request.DiaryContentRequestDto;
import com.goormthon.rememberspring.diary.api.dto.response.ChatGptResponseDto;
import com.goormthon.rememberspring.diary.api.dto.response.DiaryContentResponseDto;
import com.goormthon.rememberspring.diary.api.dto.response.DiaryResponseDto;
import com.goormthon.rememberspring.diary.domain.entity.Diary;
import com.goormthon.rememberspring.diary.domain.repository.DiaryRepository;
import com.goormthon.rememberspring.image.api.dto.response.ImageResDto;
import com.goormthon.rememberspring.image.domain.Image;
import com.goormthon.rememberspring.image.domain.repository.ImageRepository;
import com.goormthon.rememberspring.member.domain.Member;
import com.goormthon.rememberspring.member.domain.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
public class DiaryService {

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

    public DiaryService(ObjectMapper objectMapper, ImageRepository imageRepository, MemberRepository memberRepository, DiaryRepository diaryRepository) {
        this.objectMapper = objectMapper;
        this.imageRepository = imageRepository;
        this.memberRepository = memberRepository;
        this.diaryRepository = diaryRepository;
    }


    public DiaryResponseDto chat(String email, DiaryContentRequestDto diaryContentRequestDto) throws Exception {
        // 인증 시, 헤더에 실려온 토큰을 분석하여 이메일을 받아와, Member 객체 받아옴.
        Member member = memberRepository.findByEmail(email).orElse(null);
        // 아직 일기 생성이 안되었으므로, 이미지 DB의 diary_id 컬럼은  null
        List<Image> getImages = imageRepository.findByDiaryAndMember(null, member);
        List<ImageResDto> imageResDto = new ArrayList<>();

        for(Image image: getImages){
            imageResDto.add(ImageResDto.from(image));
        }

        // 리스트의 첫 번째 값(이미지)가 대표 이미지 이므로, 리스트의 첫 번째 값을 보냄.
        // 프롬프트 엔지니어링 -> 감정, 일기 타입, 음성 텍스트, 이미지를 보내, 응답값 받아옴
        ChatGptRequestDto request = new ChatGptRequestDto(model, buildQuery(imageResDto.get(0), diaryContentRequestDto));
        ChatGptResponseDto chatGptResponseDto = template.postForObject(apiURL, request, ChatGptResponseDto.class);

        DiaryContentResponseDto diaryContentResponseDto = null;
        try {
            diaryContentResponseDto =
                    objectMapper.readValue(parseJson(chatGptResponseDto), DiaryContentResponseDto.class);
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
        for(Image images : getImages) {
            images.updateImage(diary);
            imageRepository.save(images);
        }
        DiaryResponseDto diaryResponseDto = DiaryResponseDto.from(diary, imageResDto);
        return diaryResponseDto;
    }

    public DiaryResponseDto retry(String email, Long diaryId) throws Exception {
        Member member = memberRepository.findByEmail(email).orElse(null);
        Diary diary = diaryRepository.findById(diaryId).orElse(null);
        List<Image> getImages = imageRepository.findByDiaryAndMember(diary, member);

        DiaryContentRequestDto requestDto = new DiaryContentRequestDto(
                diary.getDiaryType(),
                diary.getEmotion(),
                diary.getVoiceText()
        );

        List<ImageResDto> imageResDto = new ArrayList<>();
        for(Image image: getImages){
            imageResDto.add(ImageResDto.from(image));
        }

        ChatGptRequestDto request = new ChatGptRequestDto(model, buildQuery(imageResDto.get(0), requestDto));
        ChatGptResponseDto chatGptResponseDto = template.postForObject(apiURL, request, ChatGptResponseDto.class);

        DiaryContentResponseDto diaryContentResponseDto = null;
        try {
            diaryContentResponseDto =
                    objectMapper.readValue(parseJson(chatGptResponseDto), DiaryContentResponseDto.class);
        } catch (JsonParseException e) {
            throw new RuntimeException(e);
        }
        System.out.println(diary.getContent());
        diary.updateContent(diaryContentResponseDto.getContents());
        System.out.println(diary.getContent());
        diaryRepository.save(diary);
        DiaryResponseDto diaryResponseDto = DiaryResponseDto.from(diary, imageResDto);
        return diaryResponseDto;
    }

    private String buildQuery(ImageResDto imageFile, DiaryContentRequestDto dto) throws Exception {

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
                        + "해시태그 단어 앞에는 반드시 #을 붙인다."
                        + "\ncontents는 일기 내용이며, 350자로 제한한다."
                        + "\ndate는 정해진 형식을 반환하면 된다."
                        + "\ntitle, date, hashTag, contents를 Text JSON로 변경해서 반환한다."
                        + "\n}"
                        + "\n{\ntype: image_url, image_url: {\n "+
            "url :" + imageFile.convertImageName() +"\n}"
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

    private String parseJson(ChatGptResponseDto chatGptResponseDto){
        // 백틱이 포함된 컨텐츠
        String content = chatGptResponseDto.getChoices().get(0).getMessage().getContent();
        content = content.substring(7);
        content = content.substring(0, content.length()-4);

        return content;
    }
}
