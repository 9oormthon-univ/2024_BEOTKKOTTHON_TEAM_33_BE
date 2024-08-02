package com.goormthon.rememberspring.diary.api.dto.response;

import com.goormthon.rememberspring.diary.domain.entity.Diary;
import com.goormthon.rememberspring.image.api.dto.response.ImageResDto;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.Builder;

@Builder
public record DiaryResDto(
        Long diaryId,
        boolean isPublic,
        String title,
        String createAt,
        String content,
        boolean isLike,
        int likeCount,
        List<String> hashtags,
        List<ImageResDto> imageResDtoList
) {
    public static DiaryResDto from(Diary diary) {
        List<String> hashtags = diary.getDiaryHashtagMapping().stream()
                .map(diaryHashtagMapping -> diaryHashtagMapping.getHashtag().getName())
                .toList();

        List<ImageResDto> imageResDtos = diary.getImages().stream()
                .map(ImageResDto::from)
                .toList();

        return DiaryResDto.builder()
                .diaryId(diary.getDiaryId())
                .isPublic(diary.isPublic())
                .title(diary.getTitle())
                .createAt(diary.getCreateAt().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")))
                .content(diary.getContent())
                .hashtags(hashtags)
                .imageResDtoList(imageResDtos)
                .build();
    }

    public static DiaryResDto of(Diary diary, boolean isLike, int likeCount) {
        List<String> hashtags = diary.getDiaryHashtagMapping().stream()
                .map(diaryHashtagMapping -> diaryHashtagMapping.getHashtag().getName())
                .toList();

        List<ImageResDto> imageResDtos = diary.getImages().stream()
                .map(ImageResDto::from)
                .toList();

        return DiaryResDto.builder()
                .diaryId(diary.getDiaryId())
                .isPublic(diary.isPublic())
                .title(diary.getTitle())
                .createAt(diary.getCreateAt().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")))
                .content(diary.getContent())
                .isLike(isLike)
                .likeCount(likeCount)
                .hashtags(hashtags)
                .imageResDtoList(imageResDtos)
                .build();
    }
}
