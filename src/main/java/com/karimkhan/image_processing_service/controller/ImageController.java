package com.karimkhan.image_processing_service.controller;

import com.karimkhan.image_processing_service.dto.ImageResponse;
import com.karimkhan.image_processing_service.model.Image;
import com.karimkhan.image_processing_service.model.User;
import com.karimkhan.image_processing_service.repository.UserRepository;
import com.karimkhan.image_processing_service.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;


    @PostMapping("/upload")
    public ResponseEntity<ImageResponse> uploadImage(
            @RequestParam("file") MultipartFile file) throws IOException {
        String username = getCurrentUsername();
        Image image = imageService.upload(file, username);
        return ResponseEntity.ok(ImageResponseMapper.toResponse(image));
    }

    @GetMapping
    public ResponseEntity<Page<ImageResponse>> getUserImages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String username = getCurrentUsername();
        Page<Image> images = imageService.getUserImages(username, PageRequest.of(page, size, Sort.by("uploadedAt").descending()));
        return ResponseEntity.ok(images.map(ImageResponseMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImageResponse> getImage(@PathVariable Long id) {
        String username = getCurrentUsername();
        Image image = imageService.getImageById(id, username);
        return ResponseEntity.ok(ImageResponseMapper.toResponse(image));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadImage(@PathVariable Long id) {
        String username = getCurrentUsername();
        Resource resource = imageService.downloadImage(id, username);
        Image image = imageService.getImageById(id, username);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getFormat()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getOriginalFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteImage(@PathVariable Long id) {
        String username = getCurrentUsername();
        imageService.deleteImage(id, username);
        return ResponseEntity.ok("Image deleted successfully");
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
