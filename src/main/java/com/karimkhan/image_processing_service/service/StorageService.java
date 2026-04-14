package com.karimkhan.image_processing_service.service;


import com.karimkhan.image_processing_service.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class StorageService {

    @Value("${storage.location}")
    private String storageLocation;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public String store(MultipartFile file) throws IOException {
        // Валидация файла
        validateFile(file);

        // 1. create path from storageLocation
        Path uploadDir = Paths.get(storageLocation);

        // 2. if folder doesn't exist, create it
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
            log.debug("Created upload directory: {}", uploadDir);
        }

        // 3. generate unique filename
        String extension = getExtension(file.getOriginalFilename());
        String newFilename = UUID.randomUUID() + "." + extension;

        // 4. save file to disk
        Path destination = uploadDir.resolve(newFilename);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        log.debug("Stored file: {}", newFilename);
        return newFilename;
    }

    public Path load(String filename) {
        validateFilename(filename);
        return Paths.get(storageLocation).resolve(filename);
    }

    public Resource loadAsResource(String filename) {
        try {
            Path filePath = load(filename);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("File not found or not readable: " + filename);
            }
        } catch (Exception e) {
            throw new FileStorageException("Could not load file: " + filename, e);
        }
    }

    public void delete(String filename) throws IOException {
        validateFilename(filename);
        Path path = load(filename);
        boolean deleted = Files.deleteIfExists(path);
        if (deleted) {
            log.debug("Deleted file: {}", path);
        } else {
            log.warn("File not found for deletion: {}", path);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File size exceeds maximum allowed size of 10MB");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }

        String extension = getExtension(filename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Invalid file extension. Allowed extensions: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    private void validateFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }

        // Защита от path traversal атак
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new SecurityException("Invalid filename: path traversal not allowed");
        }
    }

    private String getExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            throw new IllegalArgumentException("File has no extension: " + filename);
        }
        return filename.substring(lastDotIndex + 1);
    }
}