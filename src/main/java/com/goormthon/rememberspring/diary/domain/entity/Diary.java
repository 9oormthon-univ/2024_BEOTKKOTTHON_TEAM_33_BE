package com.goormthon.rememberspring.diary.domain.entity;

import com.goormthon.rememberspring.diary.api.dto.request.DiaryContentRequestDto;
import com.goormthon.rememberspring.diary.api.dto.response.DiaryContentResponseDto;
import com.goormthon.rememberspring.diary.config.StringListConverter;
import com.goormthon.rememberspring.image.domain.Image;
import com.goormthon.rememberspring.member.domain.Member;
import io.swagger.v3.oas.annotations.info.Info;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Diary extends BaseTimeEntity {

    // 다이어리 키
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_id")
    private Long diaryId;

    // 일기 제목
    @Column(nullable = false)
    private String title;

    // 회원 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 일기 타입
    @Enumerated(EnumType.STRING)
    private DiaryType diaryType;

    // 감정 상태
    @Enumerated(EnumType.STRING)
    private Emotion emotion;

    // 음성텍스트
    @Column(nullable = false)
    private String voiceText;

    // 글 내용
    @Column(nullable = false)
    private String content;

    // 해쉬태그
    @Convert(converter = StringListConverter.class)
    private List<String> hashTags;

    // 이미지
    @OneToMany(mappedBy = "diary",  orphanRemoval = true)
    private List<Image> images;

    public static Diary toEntity(DiaryContentResponseDto diaryContentResponseDto,
                                 DiaryContentRequestDto diaryContentRequestDto,
                                 List<Image> images,
                                 Member member) {
        return Diary.builder()
                .title(diaryContentResponseDto.getTitle())
                .member(member)
                .diaryType(diaryContentRequestDto.getDiaryType())
                .emotion(diaryContentRequestDto.getEmotion())
                .voiceText(diaryContentRequestDto.getVoiceText())
                .content(diaryContentResponseDto.getContents())
                .hashTags(diaryContentResponseDto.getHashTag())
                .images(images)
                .build();
    }
}
