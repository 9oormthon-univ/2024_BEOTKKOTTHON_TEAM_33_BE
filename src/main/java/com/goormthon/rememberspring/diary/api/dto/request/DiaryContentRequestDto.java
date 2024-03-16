package com.goormthon.rememberspring.diary.api.dto.request;

import com.goormthon.rememberspring.diary.domain.entity.DiaryType;
import com.goormthon.rememberspring.diary.domain.entity.Emotion;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class DiaryContentRequestDto implements Serializable {
    private DiaryType diaryType;
    private Emotion emotion;
    private String voiceText;
}
