package com.goormthon.rememberspring.image.api.dto.response;

import com.goormthon.rememberspring.image.domain.Image;
import lombok.Builder;

@Builder
public record ImageResDto(
        String convertImageUrl,
        int imageSequence
) {
    public static ImageResDto from(Image image) {
        return ImageResDto.builder()
                .convertImageUrl(image.getConvertImageUrl())
                .imageSequence(image.getImageSequence())
                .build();
    }
}
