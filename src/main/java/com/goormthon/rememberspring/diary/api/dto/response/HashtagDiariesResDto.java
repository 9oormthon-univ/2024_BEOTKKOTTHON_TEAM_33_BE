package com.goormthon.rememberspring.diary.api.dto.response;

import lombok.Builder;
import org.springframework.data.domain.Page;

@Builder
    public record HashtagDiariesResDto(
        String hashtagName,
        Page<DiaryResDto> diaryResDtoList
) {
    public static HashtagDiariesResDto of(String hashtagName, Page<DiaryResDto> diaryResDtoList) {
        return HashtagDiariesResDto.builder()
                .hashtagName(hashtagName)
                .diaryResDtoList(diaryResDtoList)
                .build();
    }

    public static HashtagDiariesResDto from(Page<DiaryResDto> diaryResDtoList) {
        return HashtagDiariesResDto.builder()
                .diaryResDtoList(diaryResDtoList)
                .build();
    }
}
