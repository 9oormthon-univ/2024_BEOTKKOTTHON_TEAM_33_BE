package com.goormthon.rememberspring.diary.api.controller;

import com.goormthon.rememberspring.diary.api.dto.request.DiaryContentRequestDto;
import com.goormthon.rememberspring.diary.api.dto.request.DiaryRetryRequestDto;
import com.goormthon.rememberspring.diary.api.dto.response.DiaryGeneratorResponseDto;
import com.goormthon.rememberspring.diary.api.dto.response.DiaryResDto;
import com.goormthon.rememberspring.diary.api.dto.response.HashtagDiariesResDto;
import com.goormthon.rememberspring.diary.application.DiaryGeneratorService;
import com.goormthon.rememberspring.diary.application.DiaryService;
import com.goormthon.rememberspring.global.template.RspTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryGeneratorService diaryGeneratorService;
    private final DiaryService diaryService;

    @Operation(summary = "일기 생성", description = "일기를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일기 생성 성공"),
    })
    @PostMapping(value = "/create")
    public RspTemplate<DiaryGeneratorResponseDto> createDiary(@AuthenticationPrincipal String email,
                                                              @RequestBody DiaryContentRequestDto diaryContentRequestDto) throws Exception {

        return new RspTemplate<>(HttpStatus.OK, "일기 생성", diaryGeneratorService.chat(email, diaryContentRequestDto));
    }

    @Operation(summary = "일기 생성 재요청", description = "일기를 생성을 재요청합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일기 생성 재요청 성공"),
    })
    @PostMapping(value = "/retry")
    public RspTemplate<DiaryGeneratorResponseDto> retryDiary(@AuthenticationPrincipal String email,
                                                             @RequestBody DiaryRetryRequestDto requestDto) throws Exception {
        return new RspTemplate<>(HttpStatus.OK, "일기 재생성", diaryGeneratorService.retry(email, requestDto)
        );
    }

    @Operation(summary = "다이어리 모아보기", description = "나의 다이어리를 모두 불러옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증실패", content = @Content(schema = @Schema(example = "INVALID_HEADER or INVALID_TOKEN"))),
    })
    @GetMapping("/diaries/{filterIndex}")
    public RspTemplate<List<HashtagDiariesResDto>> findAllDiaries(@AuthenticationPrincipal String email,
                                                                  @Parameter(name = "filterIndex", description = "다이어리 모아보기(ex. 1: 모아서, 2: 순서)", in = ParameterIn.PATH)
                                                                  @PathVariable(name = "filterIndex") int filterIndex,
                                                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                                                  @RequestParam(value = "size", defaultValue = "10") int size) {
        List<HashtagDiariesResDto> allDiaries;

        if (filterIndex == 1) {
            allDiaries = diaryService.gatherAllDiaries(email, page, size);
        } else {
            allDiaries = diaryService.orderAllDiaries(email, page, size);
        }

        return new RspTemplate<>(HttpStatus.OK, "조회", allDiaries);
    }

    @Operation(summary = "다이어리 상세 보기", description = "나의 다이어리 상세를 불러옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "401", description = "인증실패", content = @Content(schema = @Schema(example = "INVALID_HEADER or INVALID_TOKEN"))),
    })
    @GetMapping("/diaries")
    public RspTemplate<DiaryResDto> getDiary(@AuthenticationPrincipal String email,
                                             @RequestParam("diary") Long diaryId) {
        return new RspTemplate<>(HttpStatus.OK, "다이어리 상세 보기", diaryService.getDiary(email, diaryId));
    }

    @Operation(summary = "다이어리 공유/취소", description = "나의 다이어리를 공유(true)/취소(false) 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "401", description = "인증실패", content = @Content(schema = @Schema(example = "INVALID_HEADER or INVALID_TOKEN"))),
    })
    @PostMapping("/diaries")
    public RspTemplate<Boolean> updatePublic(@AuthenticationPrincipal String email,
                                             @RequestParam("diary") Long diaryId) {
        return new RspTemplate<>(HttpStatus.OK, "다이어리 공유/취소", diaryService.updatePublic(email, diaryId));
    }

}
