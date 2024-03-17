package com.goormthon.rememberspring.diary.api.controller;

import com.goormthon.rememberspring.diary.api.dto.request.DiaryContentRequestDto;
import com.goormthon.rememberspring.diary.api.dto.response.ChatGptResponseDto;
import com.goormthon.rememberspring.diary.api.dto.response.DiaryContentResponseDto;
import com.goormthon.rememberspring.diary.application.DiaryService;
import com.goormthon.rememberspring.global.template.RspTemplate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class DiaryController {

    @Autowired
    private DiaryService diaryService;

    @PostMapping(value = "/create")
    public RspTemplate<DiaryContentResponseDto> createDiary(
            @RequestPart MultipartFile imageFile,
            @RequestPart @Valid DiaryContentRequestDto diaryContentRequestDto) throws Exception{

        DiaryContentResponseDto response = diaryService.chat(imageFile, diaryContentRequestDto);

        return new RspTemplate<>(
                HttpStatus.OK,
                "일기 생성",
                response
                );
    }

}
