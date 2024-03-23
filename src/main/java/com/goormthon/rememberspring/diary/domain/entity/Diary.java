package com.goormthon.rememberspring.diary.domain.entity;

import com.goormthon.rememberspring.diary.api.dto.request.DiaryContentRequestDto;
import com.goormthon.rememberspring.diary.api.dto.response.DiaryContentResponseDto;
import com.goormthon.rememberspring.image.domain.Image;
import com.goormthon.rememberspring.member.domain.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
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

    // 공유 여부
    private boolean isPublic;

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
    private String voiceText;

    // 글 내용
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int likeCount;

    // 해쉬태그
    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DiaryHashtagMapping> diaryHashtagMapping = new HashSet<>();

    // 이미지
    @OneToMany(mappedBy = "diary",  orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @Builder
    public Diary(String title, boolean isPublic, Member member, DiaryType diaryType, Emotion emotion, String voiceText,
                 String content, List<Image> images) {
        this.title = title;
        this.isPublic = isPublic;
        this.member = member;
        this.diaryType = diaryType;
        this.emotion = emotion;
        this.voiceText = voiceText;
        this.content = content;
        this.likeCount = 0;
        this.images = images;
    }

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
                .images(images)
                .build();
    }

    public void addHashtagMapping(Hashtag hashtag) {
        DiaryHashtagMapping mapping = DiaryHashtagMapping.builder()
                .diary(this)
                .hashtag(hashtag)
                .build();

        this.diaryHashtagMapping.add(mapping);
    }

    public void updateDiary(String content, String voiceText) {
        this.content = content;
        this.voiceText = voiceText;
    }

    public void updateIsPublic() {
        this.isPublic = !isPublic;
    }

    public void updateLikeCount() {
        this.likeCount++;
    }

    public void updateCancelLikeCount() {
        if (this.likeCount <= 0) {
            this.likeCount = 0;
        } else {
            this.likeCount--;
        }
    }
}
