package com.goormthon.rememberspring.diary.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class DiaryContentResponseDto {
    private String title;
    private String date;
    private List<String> hashTag;
    private String contents;
}
