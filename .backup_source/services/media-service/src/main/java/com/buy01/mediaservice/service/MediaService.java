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

    public MediaService(MediaRepository mediaRepository, @Value("${file.upload-dir}") String uploadDir) {
        this.mediaRepository = mediaRepository;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public Media uploadMedia(MultipartFile file, String uploaderId) throws IOException {
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IOException("File size exceeds the limit of 2MB");
        }
        if (!file.getContentType().startsWith("image/")) {
            throw new IOException("Only image files are allowed");
        }

        String fileName = UUID.randomUUID().toString() + "_" + StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if (fileName.contains("..")) {
                throw new IOException("Filename contains invalid path sequence " + fileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            Media media = new Media(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    targetLocation.toString(),
                    uploaderId);

            return mediaRepository.save(media);
        } catch (IOException ex) {
            throw new IOException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Media getMedia(String id) {
        return mediaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Media not found"));
    }

    public byte[] getMediaData(String id) throws IOException {
        Media media = getMedia(id);
        Path filePath = Paths.get(media.getFilePath());
        return Files.readAllBytes(filePath);
    }

    public List<Media> getMediaByUploader(String uploaderId) {
        return mediaRepository.findByUploaderId(uploaderId);
    }
}
