package com.goormthon.rememberspring.diary.api.controller;

import com.goormthon.rememberspring.diary.api.dto.request.DiaryContentRequestDto;
import com.goormthon.rememberspring.diary.application.DiaryService;
import com.goormthon.rememberspring.global.template.RspTemplate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class DiaryController {

    @Autowired
    private DiaryService diaryService;

    @PostMapping(value = "/create")
    public RspTemplate<?> createDiary(
            @RequestPart List<MultipartFile> files,
            @RequestPart @Valid DiaryContentRequestDto diaryContentRequestDto) throws Exception{

        return new RspTemplate<>(HttpStatus.OK, "일기 생성", diaryService.chat(files, diaryContentRequestDto));
    }
}
