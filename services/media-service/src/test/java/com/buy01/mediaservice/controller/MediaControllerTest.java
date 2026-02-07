package com.buy01.mediaservice.controller;

import com.buy01.mediaservice.model.Media;
import com.buy01.mediaservice.service.MediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MediaControllerTest {

    private static final String TEST_FILENAME = "test.png";
    private static final String TEST_CONTENT_TYPE = "image/png";
    private static final String TEST_USER = "user1";

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private MediaController mediaController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(mediaController).build();
    }

    @Test
    void uploadMedia_shouldReturnOk() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", TEST_FILENAME, TEST_CONTENT_TYPE, "content".getBytes());

        Media mockMedia = new Media(TEST_FILENAME, TEST_CONTENT_TYPE, "uuid_" + TEST_FILENAME, TEST_USER);
        when(mediaService.uploadMedia(any(), eq(TEST_USER))).thenReturn(mockMedia);

        Principal mockPrincipal = () -> TEST_USER;

        mockMvc.perform(multipart("/api/media/upload")
                .file(file)
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(TEST_FILENAME));
    }

    @Test
    void getMedia_shouldReturnFileContent() throws Exception {
        String mediaId = "123";
        Media mockMedia = new Media(TEST_FILENAME, TEST_CONTENT_TYPE, "uuid_" + TEST_FILENAME, TEST_USER);
        byte[] content = "file-content".getBytes(StandardCharsets.UTF_8);

        when(mediaService.getMedia(mediaId)).thenReturn(mockMedia);
        when(mediaService.getMediaData(mediaId)).thenReturn(content);

        mockMvc.perform(get("/api/media/{id}", mediaId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "inline; filename=\"" + TEST_FILENAME + "\""))
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(content));
    }

    @Test
    void getSellerMedia_shouldReturnList() throws Exception {
        Principal mockPrincipal = () -> "seller1";
        when(mediaService.getMediaByUploader("seller1")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/media/seller")
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
