package com.buy01.mediaservice.repository;

import com.buy01.mediaservice.model.Media;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MediaRepository extends MongoRepository<Media, String> {
    List<Media> findByUploaderId(String uploaderId);
}
