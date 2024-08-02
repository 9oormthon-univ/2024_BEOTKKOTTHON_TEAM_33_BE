package com.goormthon.rememberspring.diary.api.dto.request;

import com.goormthon.rememberspring.diary.domain.entity.DiaryType;
import com.goormthon.rememberspring.diary.domain.entity.Emotion;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiaryContentRequestDto implements Serializable {
    private DiaryType diaryType;
    private Emotion emotion;
    private String voiceText;

    @Builder
    public DiaryContentRequestDto(DiaryType diaryType, Emotion emotion, String voiceText) {
        this.diaryType = diaryType;
        this.emotion = emotion;
        this.voiceText = voiceText;
    }
}
