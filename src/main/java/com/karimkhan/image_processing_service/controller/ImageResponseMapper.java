package com.karimkhan.image_processing_service.controller;

import com.karimkhan.image_processing_service.dto.ImageResponse;
import com.karimkhan.image_processing_service.model.Image;

public class ImageResponseMapper {
    public static ImageResponse toResponse(Image image) {
        return ImageResponse.builder()
                .id(image.getId())
                .originalFilename(image.getOriginalFilename())
                .format(image.getFormat())
                .fileSize(image.getFileSize())
                .width(image.getWidth())
                .height(image.getHeight())
                .uploadedAt(image.getUploadedAt())
                .username(image.getUser().getUsername())
                .build();
    }
}
