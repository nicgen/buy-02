package com.buy01.mediaservice.service;

import com.buy01.mediaservice.model.Media;
import com.buy01.mediaservice.repository.MediaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class MediaService {

    private final MediaRepository mediaRepository;
    private final Path fileStorageLocation;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MediaService.class);

    public MediaService(MediaRepository mediaRepository, @Value("${file.upload-dir}") String uploadDir) {
        this.mediaRepository = mediaRepository;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new java.io.UncheckedIOException(
                    "Could not create the directory where the uploaded files will be stored.", new IOException(ex));
        }
    }

    public Media uploadMedia(MultipartFile file, String uploaderId) throws IOException {
        logger.info("Starting uploadMedia: filename={}, size={}, contentType={}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        if (file.getSize() > 8 * 1024 * 1024) {
            logger.error("File size exceeded: {} bytes", file.getSize());
            throw new IOException("File size exceeds the limit of 8MB");
        }

        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        boolean isValidImage = false;

        // 1. Check Content-Type header
        if (contentType != null && contentType.startsWith("image/")) {
            isValidImage = true;
        }

        // 2. Fallback: check extension if Content-Type is missing or generic
        if (!isValidImage && originalFilename != null) {
            String lowerName = originalFilename.toLowerCase();
            if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") ||
                    lowerName.endsWith(".png") || lowerName.endsWith(".gif") ||
                    lowerName.endsWith(".webp") || lowerName.endsWith(".bmp")) {
                logger.info("Validating by extension for filename: {}", originalFilename);
                isValidImage = true;
            }
        }

        if (!isValidImage) {
            logger.error("Invalid file type: contentType={}, filename={}", contentType, originalFilename);
            throw new IOException("Only image files are allowed");
        }

        String fileName = UUID.randomUUID().toString() + "_" + StringUtils.cleanPath(originalFilename);

        try {
            if (fileName.contains("..")) {
                throw new IOException("Filename contains invalid path sequence " + fileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File stored successfully at: {}", targetLocation);

            // CHANGED: Store only the fileName (relative path) to make it portable.
            // The getMediaData method will resolve it against the current
            // fileStorageLocation.
            Media media = new Media(
                    originalFilename,
                    contentType,
                    fileName,
                    uploaderId);

            return mediaRepository.save(media);
        } catch (IOException ex) {
            logger.error("Failed to store file " + fileName, ex);
            throw new IOException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Media getMedia(String id) {
        return mediaRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Media not found"));
    }

    public byte[] getMediaData(String id) throws IOException {
        Media media = getMedia(id);

        // Robust path resolution:
        // 1. Try resolving as relative path (standard for new uploads)
        // 2. If stored as absolute (legacy), check if exists. If not, try to extract
        // filename and look in current storage.

        Path filePath;
        String storedPath = media.getFilePath();
        Path storedPathObj = Paths.get(storedPath);

        if (storedPathObj.isAbsolute()) {
            if (Files.exists(storedPathObj)) {
                filePath = storedPathObj;
            } else {
                // Fallback: assume it's a legacy absolute path that moved. Extract filename.
                String filename = storedPathObj.getFileName().toString();
                filePath = this.fileStorageLocation.resolve(filename);
            }
        } else {
            // It's a relative path (filename)
            filePath = this.fileStorageLocation.resolve(storedPath);
        }

        if (!Files.exists(filePath)) {
            // Final fallback for "repaired" references or lost files -> placeholder if
            // exists, else error
            // Ideally we throw error, but for robustness we could log. Sticking to
            // IOException for now.
            throw new IOException("File not found at " + filePath);
        }

        return Files.readAllBytes(filePath);
    }

    public List<Media> getMediaByUploader(String uploaderId) {
        return mediaRepository.findByUploaderId(uploaderId);
    }
}
