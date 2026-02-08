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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaServiceValidationTest {

    @Mock
    private MediaRepository mediaRepository;

    private MediaService mediaService;

    @TempDir
    Path tempDir;

    private static final String USER_ID = "user123";
    private static final String IMAGE_PNG = "image/png";
    private static final byte[] CONTENT = "content".getBytes();

    @BeforeEach
    void setUp() {
        mediaService = new MediaService(mediaRepository, tempDir.toString());
    }

    @Test
    void uploadMediaShouldSucceedAvoidContentTypeCheckForImages() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                IMAGE_PNG,
                CONTENT);

        when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Media media = mediaService.uploadMedia(file, USER_ID);
        assertNotNull(media);
        assertEquals(IMAGE_PNG, media.getType());
    }

    @Test
    void uploadMediaShouldSucceedWithGenericContentTypeAndValidExtension() throws IOException {
        // This simulates the "Fix": Content-Type is generic stream, but extension is
        // .png
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "screenshot.png",
                "application/octet-stream",
                CONTENT);

        when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Media media = mediaService.uploadMedia(file, USER_ID);
        assertNotNull(media);
        assertEquals("application/octet-stream", media.getType());
    }

    @Test
    void uploadMediaShouldFailWithInvalidExtensionAndGenericType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                CONTENT);

        IOException exception = assertThrows(IOException.class, () -> {
            mediaService.uploadMedia(file, USER_ID);
        });

        assertEquals("Only image files are allowed", exception.getMessage());
    }

    @Test
    void uploadMediaShouldFailIfTooLarge() {
        // Create a file slightly larger than 8MB
        byte[] largeContent = new byte[(8 * 1024 * 1024) + 100];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.png",
                IMAGE_PNG,
                largeContent);

        IOException exception = assertThrows(IOException.class, () -> {
            mediaService.uploadMedia(file, USER_ID);
        });

        assertTrue(exception.getMessage().contains("File size exceeds the limit"));
    }
}
