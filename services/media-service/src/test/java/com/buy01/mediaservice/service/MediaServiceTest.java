package com.buy01.mediaservice.service;

import com.buy01.mediaservice.model.Media;
import com.buy01.mediaservice.repository.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    private MediaService mediaService;

    private static final String IMAGE_PNG = "image/png";
    private static final String USER_ID = "user1";

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        mediaService = new MediaService(mediaRepository, tempDir.toString());
    }

    @Test
    void uploadMediaShouldStoreRelativePath() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.png",
                IMAGE_PNG,
                "test content".getBytes());

        when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Media savedMedia = mediaService.uploadMedia(file, USER_ID);

        assertNotNull(savedMedia);
        // CRITICAL: Verify we are storing only the filename (relative path), not the
        // absolute path
        // Service prepends UUID, so we check endsWith
        assertTrue(savedMedia.getFilePath().endsWith("test-image.png"));
        assertTrue(Files.exists(tempDir.resolve(savedMedia.getFilePath())));
    }

    @Test
    void getMediaDataShouldResolveRelativePath() throws IOException {
        String filename = "relative.png";
        Path filePath = tempDir.resolve(filename);
        Files.write(filePath, "content".getBytes());

        Media media = new Media("relative.png", IMAGE_PNG, filename, USER_ID);
        when(mediaRepository.findById("1")).thenReturn(Optional.of(media));

        byte[] data = mediaService.getMediaData("1");

        assertArrayEquals("content".getBytes(), data);
    }

    @Test
    void getMediaDataShouldFallbackToRelativeWhenAbsolutePathIsBroken() throws IOException {
        // This simulates the "Repair" scenario:
        // DB has an absolute path from an old deployment: /old/app/uploads/legacy.png
        // But the file actually exists in our current storage at: tempDir/legacy.png

        String filename = "legacy.png";
        String oldAbsolutePath = "/old/app/uploads/" + filename;

        // Create the file in the CURRENT valid location
        Path realFilePath = tempDir.resolve(filename);
        Files.write(realFilePath, "legacy content".getBytes());

        Media media = new Media("Legacy", IMAGE_PNG, oldAbsolutePath, USER_ID);
        when(mediaRepository.findById("2")).thenReturn(Optional.of(media));

        byte[] data = mediaService.getMediaData("2");

        assertArrayEquals("legacy content".getBytes(), data);
    }

    @Test
    void getMediaDataShouldThrowExceptionWhenFileNotFound() {
        Media media = new Media("Missing", IMAGE_PNG, "missing.png", USER_ID);
        when(mediaRepository.findById("3")).thenReturn(Optional.of(media));

        assertThrows(IOException.class, () -> mediaService.getMediaData("3"));
    }
}
