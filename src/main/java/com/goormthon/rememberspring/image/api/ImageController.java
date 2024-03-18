package com.goormthon.rememberspring.image.api;

import com.goormthon.rememberspring.global.template.RspTemplate;
import com.goormthon.rememberspring.image.application.ImageService;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping("/upload")
    public RspTemplate<List<String>> registerUser(
            @AuthenticationPrincipal String email,
            @RequestPart("multipartFiles") MultipartFile[] multipartFiles) throws IOException {
        return new RspTemplate<>(HttpStatus.OK, "이미지 업로드 성공", imageService.upload(email, multipartFiles));
    }
}
