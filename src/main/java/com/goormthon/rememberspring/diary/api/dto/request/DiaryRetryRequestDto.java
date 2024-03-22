package com.goormthon.rememberspring.diary.api.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class DiaryRetryRequestDto implements Serializable {
    private Long diaryId;
    private String voiceText;

    @Builder
    public DiaryRetryRequestDto(Long diaryId, String voiceText) {
        this.diaryId = diaryId;
        this.voiceText = voiceText;
    }
}
