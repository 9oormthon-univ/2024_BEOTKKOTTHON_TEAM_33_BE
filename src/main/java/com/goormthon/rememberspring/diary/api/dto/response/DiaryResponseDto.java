package com.goormthon.rememberspring.diary.api.dto.response;

import com.goormthon.rememberspring.diary.domain.entity.Diary;
import com.goormthon.rememberspring.image.api.dto.response.ImageResDto;
import com.goormthon.rememberspring.member.domain.Member;
import lombok.Builder;

import java.util.List;

@Builder
public record DiaryResponseDto (
        Long diaryId,
        String title,
        String content,
        List<String> hashTags,
        List<ImageResDto> images
) {
    public static DiaryResponseDto from(Diary diary, List<ImageResDto> imageResDto) {
        return DiaryResponseDto.builder()
                .diaryId(diary.getDiaryId())
                .title(diary.getTitle())
                .content(diary.getContent())
                .hashTags(diary.getHashTags())
                .images(imageResDto)
                .build();
    }

}
