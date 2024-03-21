package com.goormthon.rememberspring.diary.api.dto.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiaryContentResponseDto {
    private String title;
    private String date;
    private List<String> hashtag;
    private String contents;
}
