package com.goormthon.rememberspring.diary.api.dto.response;

import lombok.Builder;
import org.springframework.data.domain.Page;

@Builder
public record HashtagDiariesResDto(
        String hashtagName,
        Page<DiaryResDto> diaryResponseDtoList
) {
    public static HashtagDiariesResDto of(String hashtagName, Page<DiaryResDto> diaryResponseDtoList) {
        return HashtagDiariesResDto.builder()
                .hashtagName(hashtagName)
                .diaryResponseDtoList(diaryResponseDtoList)
                .build();
    }
}
