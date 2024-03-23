package com.goormthon.rememberspring.diary.api.controller;

import com.goormthon.rememberspring.diary.api.dto.response.DiaryResDto;
import com.goormthon.rememberspring.diary.api.dto.response.HashtagDiariesResDto;
import com.goormthon.rememberspring.diary.api.dto.response.PublicDiaryResDto;
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
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user/diaries")
public class DiaryController {

    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    @Operation(summary = "다이어리 모아보기", description = "나의 다이어리를 모두 불러옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증실패", content = @Content(schema = @Schema(example = "INVALID_HEADER or INVALID_TOKEN"))),
    })
    @GetMapping("/{filterIndex}")
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
    @GetMapping("/detail/{diaryId}")
    public RspTemplate<DiaryResDto> getDiary(@AuthenticationPrincipal String email,
                                             @PathVariable("diaryId") Long diaryId) {
        return new RspTemplate<>(HttpStatus.OK, "다이어리 상세 보기", diaryService.getDiary(email, diaryId));
    }

    @Operation(summary = "다이어리 공유/취소", description = "나의 다이어리를 공유(true)/취소(false) 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "401", description = "인증실패", content = @Content(schema = @Schema(example = "INVALID_HEADER or INVALID_TOKEN"))),
    })
    @PostMapping("/detail")
    public RspTemplate<Boolean> updatePublic(@AuthenticationPrincipal String email,
                                             @RequestParam("diaryId") Long diaryId) {
        return new RspTemplate<>(HttpStatus.OK, "다이어리 공유/취소", diaryService.updatePublic(email, diaryId));
    }

    @Operation(summary = "다이어리 함께보기", description = "공유된 다이어리를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증실패", content = @Content(schema = @Schema(example = "INVALID_HEADER or INVALID_TOKEN"))),
    })
    @GetMapping("/public")
    public RspTemplate<Page<PublicDiaryResDto>> publicDiaries(@AuthenticationPrincipal String email,
                                                              @Parameter(name = "filter", description = "정렬 기준 ex) 1: 최신순, 2: 인기순", in = ParameterIn.QUERY)
                                                              @RequestParam(value = "filter") int filter,
                                                              @Parameter(name = "year", description = "년 ex) 2024", in = ParameterIn.QUERY)
                                                              @RequestParam(value = "year") int year,
                                                              @Parameter(name = "month", description = "월 ex) 3", in = ParameterIn.QUERY)
                                                              @RequestParam(value = "month") int month,
                                                              @RequestParam(value = "page", defaultValue = "0") int page,
                                                              @RequestParam(value = "size", defaultValue = "10") int size) {
        String sortProperty = filter == 1 ? "diaryId" : "likeCount";

        return new RspTemplate<>(HttpStatus.OK, "공유된 다이어리 조회", diaryService.publicAllDiaries(email, sortProperty, year, month, page, size));
    }

    @Operation(summary = "다이어리 함께보기 상세 보기", description = "함께보기 다이어리 상세를 불러옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "401", description = "인증실패", content = @Content(schema = @Schema(example = "INVALID_HEADER or INVALID_TOKEN"))),
    })
    @GetMapping("/public/detail/{diaryId}")
    public RspTemplate<PublicDiaryResDto> getPublicDiary(@AuthenticationPrincipal String email,
                                                         @PathVariable("diaryId") Long diaryId) {
        return new RspTemplate<>(HttpStatus.OK, "다이어리 상세 보기", diaryService.getPublicDiary(email, diaryId));
    }

    @Operation(summary = "다이어리 좋아요", description = "다이어리 좋아요를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "401", description = "인증실패", content = @Content(schema = @Schema(example = "INVALID_HEADER or INVALID_TOKEN"))),
    })
    @PostMapping("/public/detail")
    public RspTemplate<String> updateLikeDiary(@AuthenticationPrincipal String email,
                                             @RequestParam Long diaryId) {
        diaryService.likeDiary(email, diaryId);
        return new RspTemplate<>(HttpStatus.OK, "다이어리 좋아요", "다이어리 좋아요 성공");
    }

    @Operation(summary = "다이어리 좋아요 취소", description = "다이어리 좋아요를 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "401", description = "인증실패", content = @Content(schema = @Schema(example = "INVALID_HEADER or INVALID_TOKEN"))),
    })
    @DeleteMapping("/public/detail")
    public RspTemplate<String> updateCancelLikeDiary(@AuthenticationPrincipal String email,
                                                     @RequestParam Long diaryId) {
        diaryService.cancelLikeDiary(email, diaryId);
        return new RspTemplate<>(HttpStatus.OK, "다이어리 좋아요 취소", "다이어리 좋아요 취소 성공");
    }

}
