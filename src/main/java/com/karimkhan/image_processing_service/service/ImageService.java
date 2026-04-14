package com.karimkhan.image_processing_service.service;


import com.karimkhan.image_processing_service.exception.InvalidFileException;
import com.karimkhan.image_processing_service.exception.ResourceNotFoundException;
import com.karimkhan.image_processing_service.exception.UnauthorizedException;
import com.karimkhan.image_processing_service.model.Image;
import com.karimkhan.image_processing_service.model.User;
import com.karimkhan.image_processing_service.repository.ImageRepository;
import com.karimkhan.image_processing_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    );

    @Transactional
    public Image upload(MultipartFile file, String username) throws IOException {
        // Валидация файла
        validateFile(file);

        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String storedFilename = null;
        try {
            // Сохранение файла в хранилище
            storedFilename = storageService.store(file);

            // Чтение изображения для получения размеров
            BufferedImage buffered = ImageIO.read(file.getInputStream());
            if (buffered == null) {
                throw new InvalidFileException("Invalid image format. Unable to read image file.");
            }

            // Создание и сохранение записи в БД
            Image image = new Image();
            image.setOriginalFilename(file.getOriginalFilename());
            image.setStoredFilename(storedFilename);
            image.setFilePath(storageService.load(storedFilename).toString());
            image.setFormat(file.getContentType());
            image.setFileSize(file.getSize());
            image.setUser(user);
            image.setWidth(buffered.getWidth());
            image.setHeight(buffered.getHeight());

            Image savedImage = imageRepository.save(image);
            log.info("Image uploaded successfully: {} by user {}", savedImage.getOriginalFilename(), username);
            return savedImage;
        } catch (ResourceNotFoundException | InvalidFileException e) {
            throw e;
        } catch (Exception e) {
            // Очистка сохраненного файла при ошибке
            if (storedFilename != null) {
                try {
                    storageService.delete(storedFilename);
                    log.warn("Cleaned up stored file after error: {}", storedFilename);
                } catch (IOException ex) {
                    log.error("Failed to cleanup stored file: {}", storedFilename, ex);
                }
            }
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<Image> getUserImages(String username, Pageable pageable) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return imageRepository.findByUser(user, pageable);
    }

    @Transactional(readOnly = true)
    public Image getImageById(Long id, String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return imageRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));
    }

    @Transactional(readOnly = true)
    public Resource downloadImage(Long id, String username) {
        Image image = getImageById(id, username);
        return storageService.loadAsResource(image.getStoredFilename());
    }

    @Transactional
    public void deleteImage(Long id, String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Image image = imageRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));

        // Удаляем файл из хранилища
        try {
            storageService.delete(image.getStoredFilename());
        } catch (IOException e) {
            log.error("Failed to delete file from storage: {}", image.getStoredFilename(), e);
        }

        // Удаляем запись из БД
        imageRepository.delete(image);
        log.info("Image deleted successfully: {} by user {}", image.getOriginalFilename(), username);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File cannot be empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new InvalidFileException("Filename cannot be empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidFileException(
                    "Invalid file type. Allowed types: " + String.join(", ", ALLOWED_CONTENT_TYPES));
        }

        log.debug("File validation passed for: {}", originalFilename);
    }
}
