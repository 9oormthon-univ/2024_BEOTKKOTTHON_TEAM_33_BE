package com.goormthon.rememberspring.diary.api.dto.response;

import com.goormthon.rememberspring.diary.domain.entity.Diary;
import com.goormthon.rememberspring.image.api.dto.response.ImageResDto;
import java.util.List;
import lombok.Builder;

@Builder
public record DiaryResponseDto (
        Long diaryId,
        String title,
        String content,
        List<String> hashtags,
        List<ImageResDto> images
) {
    public static DiaryResponseDto from(Diary diary, List<ImageResDto> imageResDto) {
        List<String> hashtags = diary.getDiaryHashtagMapping().stream()
                .map(diaryHashtagMapping -> diaryHashtagMapping.getHashtag().getName())
                .toList();

        return DiaryResponseDto.builder()
                .diaryId(diary.getDiaryId())
                .title(diary.getTitle())
                .content(diary.getContent())
                .hashtags(hashtags)
                .images(imageResDto)
                .build();
    }

}
