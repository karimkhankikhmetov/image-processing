package com.karimkhan.image_processing_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageResponse {
    private Long id;
    private String originalFilename;
    private String format;
    private Long fileSize;
    private Integer width;
    private Integer height;
    private LocalDateTime uploadedAt;
    private String username;
}
