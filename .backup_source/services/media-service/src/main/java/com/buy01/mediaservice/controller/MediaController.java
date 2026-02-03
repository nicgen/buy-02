package com.buy01.mediaservice.controller;

import com.buy01.mediaservice.model.Media;
import com.buy01.mediaservice.service.MediaService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Media> uploadMedia(@RequestParam("file") MultipartFile file, Principal principal)
            throws IOException {
        return ResponseEntity.ok(mediaService.uploadMedia(file, principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getMedia(@PathVariable String id) throws IOException {
        Media media = mediaService.getMedia(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + media.getName() + "\"")
                .contentType(MediaType.parseMediaType(media.getType()))
                .body(mediaService.getMediaData(id));
    }

    @GetMapping("/seller")
    public ResponseEntity<List<Media>> getSellerMedia(Principal principal) {
        return ResponseEntity.ok(mediaService.getMediaByUploader(principal.getName()));
    }
}
