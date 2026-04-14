package com.karimkhan.image_processing_service.repository;

import com.karimkhan.image_processing_service.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.karimkhan.image_processing_service.model.Image;

import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    Page<Image> findByUser(User user, Pageable pageable);
    Optional<Image> findByIdAndUser(Long id, User user);
}
