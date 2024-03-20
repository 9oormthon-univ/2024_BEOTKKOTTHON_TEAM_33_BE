package com.goormthon.rememberspring.diary.api.controller;

import com.goormthon.rememberspring.diary.api.dto.request.DiaryContentRequestDto;
import com.goormthon.rememberspring.diary.api.dto.response.DiaryResponseDto;
import com.goormthon.rememberspring.diary.application.DiaryGeneratorService;
import com.goormthon.rememberspring.global.template.RspTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class DiaryController {

    @Autowired
    private DiaryGeneratorService diaryGeneratorService;

    @Operation(summary = "일기 생성", description = "일기를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일기 생성 성공"),
    })
    @PostMapping(value = "/create")
    public RspTemplate<DiaryResponseDto> createDiary(
            @AuthenticationPrincipal String email,
            @RequestBody DiaryContentRequestDto diaryContentRequestDto) throws Exception{


        return new RspTemplate<>(
                HttpStatus.OK,
                "일기 생성",
                diaryGeneratorService.chat(email, diaryContentRequestDto)
                );
    }

    @Operation(summary = "일기 생성 재요청", description = "일기를 생성을 재요청합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일기 생성 재요청 성공"),
    })
    @PostMapping(value = "/retry")
    public RspTemplate<DiaryResponseDto> retryDiary(
            @AuthenticationPrincipal String email) throws Exception{
        return new RspTemplate<>(
                HttpStatus.OK,
                "일기 생성",
                diaryGeneratorService.retry(email)
        );
    }

}
