package com.gege.activitypartner.service;

import com.gege.activitypartner.dto.CoverImageDTO;
import com.gege.activitypartner.entity.CoverImage;
import com.gege.activitypartner.repository.CoverImageRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class CoverImageService {

  private final CoverImageRepository coverImageRepository;
  private final CoverImageStorageService coverImageStorageService;

  @Transactional(readOnly = true)
  public List<CoverImageDTO> getAllAvailableCoverImages() {
    return coverImageRepository.findByActiveTrueOrderByDisplayNameAsc().stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  public CoverImageDTO uploadCoverImage(MultipartFile file, String displayName) {
    String fileName = coverImageStorageService.storeFile(file);
    String imageUrl = "/api/covers/" + fileName;

    CoverImage coverImage = new CoverImage();
    coverImage.setFileName(fileName);
    coverImage.setImageUrl(imageUrl);
    coverImage.setDisplayName(displayName);
    coverImage.setActive(true);

    CoverImage saved = coverImageRepository.save(coverImage);
    return mapToDTO(saved);
  }

  public void deactivateCoverImage(Long id) {
    coverImageRepository
        .findById(id)
        .ifPresent(
            coverImage -> {
              coverImage.setActive(false);
              coverImageRepository.save(coverImage);
            });
  }

  private CoverImageDTO mapToDTO(CoverImage image) {
    return new CoverImageDTO(image.getId(), image.getImageUrl(), image.getDisplayName());
  }
}
