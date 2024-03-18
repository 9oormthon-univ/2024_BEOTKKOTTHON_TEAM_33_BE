package com.goormthon.rememberspring.image.api.dto.response;

import com.goormthon.rememberspring.image.domain.Image;
import lombok.Builder;

@Builder
public record ImageResDto(
        String convertImageName,
        int imageSequence
) {
    public static ImageResDto from(Image image) {
        return ImageResDto.builder()
                .convertImageName(image.getConvertImageName())
                .imageSequence(image.getImageSequence())
                .build();
    }
}
