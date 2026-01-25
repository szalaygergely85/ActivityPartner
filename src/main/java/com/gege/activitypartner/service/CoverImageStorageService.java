package com.gege.activitypartner.service;

import com.gege.activitypartner.exception.FileStorageException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CoverImageStorageService {

  private final Path fileStorageLocation;

  public CoverImageStorageService(@Value("${file.cover-images.dir}") String uploadDir) {
    this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

    try {
      Files.createDirectories(this.fileStorageLocation);
    } catch (Exception ex) {
      throw new FileStorageException("Could not create the directory for cover images.", ex);
    }
  }

  public String storeFile(MultipartFile file) {
    String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

    try {
      if (originalFileName.contains("..")) {
        throw new FileStorageException(
            "Filename contains invalid path sequence: " + originalFileName);
      }

      String contentType = file.getContentType();
      if (contentType == null || !contentType.startsWith("image/")) {
        throw new FileStorageException("Only image files are allowed");
      }

      String fileExtension = "";
      int dotIndex = originalFileName.lastIndexOf('.');
      if (dotIndex > 0) {
        fileExtension = originalFileName.substring(dotIndex);
      }
      String fileName = UUID.randomUUID().toString() + fileExtension;

      Path targetLocation = this.fileStorageLocation.resolve(fileName);
      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

      return fileName;
    } catch (IOException ex) {
      throw new FileStorageException(
          "Could not store file " + originalFileName + ". Please try again!", ex);
    }
  }

  public void deleteFile(String fileName) {
    try {
      Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
      Files.deleteIfExists(filePath);
    } catch (IOException ex) {
      throw new FileStorageException("Could not delete file " + fileName, ex);
    }
  }

  public Path getFileStorageLocation() {
    return fileStorageLocation;
  }
}
