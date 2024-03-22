package com.goormthon.rememberspring.image.api;

import com.goormthon.rememberspring.global.template.RspTemplate;
import com.goormthon.rememberspring.image.api.dto.response.ImageResDto;
import com.goormthon.rememberspring.image.application.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/user")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @Operation(summary = "이미지 업로드", description = "이미지 업로드 합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "업로드 성공"),
            @ApiResponse(responseCode = "401", description = "인증실패", content = @Content(schema = @Schema(example = "INVALID_HEADER or INVALID_TOKEN"))),
    })
    @PostMapping("/upload")
    public RspTemplate<List<ImageResDto>> imageUpload(@AuthenticationPrincipal String email,
                                                 @RequestPart("multipartFiles") MultipartFile[] multipartFiles) throws IOException {
        return new RspTemplate<>(HttpStatus.OK, "이미지 업로드", imageService.upload(email, multipartFiles));
    }

    @Operation(summary = "이미지 URL 업로드", description = "이미지 URL 업로드 합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "업로드 성공"),
            @ApiResponse(responseCode = "401", description = "인증실패", content = @Content(schema = @Schema(example = "INVALID_HEADER or INVALID_TOKEN"))),
    })
    @PostMapping("/upload/url")
    public RspTemplate<List<ImageResDto>> imageURLUpload(@AuthenticationPrincipal String email,
                                                      @RequestBody List<String> imageUrls) throws IOException {
        return new RspTemplate<>(HttpStatus.OK, "이미지 업로드", imageService.urlUpload(email, imageUrls));
    }

    @Operation(summary = "이미지 전체 조회", description = "이미지를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증실패", content = @Content(schema = @Schema(example = "INVALID_HEADER or INVALID_TOKEN"))),
    })
    @GetMapping("/images")
    public RspTemplate<List<ImageResDto>> images(@AuthenticationPrincipal String email) {
        List<ImageResDto> images = imageService.images(email);

        return new RspTemplate<>(HttpStatus.OK, "이미지 조회", images);
    }

}
