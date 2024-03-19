package com.goormthon.rememberspring.diary.api.controller;

import com.goormthon.rememberspring.diary.api.dto.request.DiaryContentRequestDto;
import com.goormthon.rememberspring.diary.api.dto.response.ChatGptResponseDto;
import com.goormthon.rememberspring.diary.api.dto.response.DiaryContentResponseDto;
import com.goormthon.rememberspring.diary.application.DiaryService;
import com.goormthon.rememberspring.diary.domain.entity.Diary;
import com.goormthon.rememberspring.global.template.RspTemplate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class DiaryController {

    @Autowired
    private DiaryService diaryService;

    @PostMapping(value = "/create")
    public RspTemplate<Diary> createDiary(
            @AuthenticationPrincipal String email,
            @RequestPart @Valid DiaryContentRequestDto diaryContentRequestDto) throws Exception{


        Diary diary = diaryService.chat(email, diaryContentRequestDto);
        return new RspTemplate<>(
                HttpStatus.OK,
                "일기 생성",
                diary
                );
    }

}
