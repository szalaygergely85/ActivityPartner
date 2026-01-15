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
public class FileStorageService {

  private final Path fileStorageLocation;

  public FileStorageService(@Value("${file.upload.dir}") String uploadDir) {
    this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

    try {
      Files.createDirectories(this.fileStorageLocation);
    } catch (Exception ex) {
      throw new FileStorageException(
          "Could not create the directory where uploaded files will be stored.", ex);
    }
  }

  /** Store a file and return the file path */
  public String storeFile(MultipartFile file) {
    // Normalize file name
    String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

    try {
      // Check if the file's name contains invalid characters
      if (originalFileName.contains("..")) {
        throw new FileStorageException(
            "Filename contains invalid path sequence: " + originalFileName);
      }

      // Validate file type (only images)
      String contentType = file.getContentType();
      if (contentType == null || !contentType.startsWith("image/")) {
        throw new FileStorageException("Only image files are allowed");
      }

      // Generate unique filename to avoid collisions
      String fileExtension = "";
      int dotIndex = originalFileName.lastIndexOf('.');
      if (dotIndex > 0) {
        fileExtension = originalFileName.substring(dotIndex);
      }
      String fileName = UUID.randomUUID().toString() + fileExtension;

      // Copy file to the target location (Replacing existing file with the same name)
      Path targetLocation = this.fileStorageLocation.resolve(fileName);
      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

      return fileName;
    } catch (IOException ex) {
      throw new FileStorageException(
          "Could not store file " + originalFileName + ". Please try again!", ex);
    }
  }

  /** Delete a file by filename */
  public void deleteFile(String fileName) {
    try {
      Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
      Files.deleteIfExists(filePath);
    } catch (IOException ex) {
      throw new FileStorageException("Could not delete file " + fileName, ex);
    }
  }

  /** Get the file storage location */
  public Path getFileStorageLocation() {
    return fileStorageLocation;
  }
}
