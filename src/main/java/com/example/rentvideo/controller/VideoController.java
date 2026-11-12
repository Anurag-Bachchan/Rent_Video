package com.example.rentvideo.controller;

import com.example.rentvideo.dto.VideoRequest;
import com.example.rentvideo.entity.Video;
import com.example.rentvideo.service.VideoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    // CUSTOMER or ADMIN
    @GetMapping
    public List<Video> getAvailableVideos() {
        return videoService.getAvailableVideos();
    }

    // CUSTOMER or ADMIN
    @GetMapping("/{id}")
    public Video getVideo(@PathVariable Long id) {
        return videoService.getById(id);
    }

    // ADMIN only
    @PostMapping
    public ResponseEntity<Video> createVideo(@Valid @RequestBody VideoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(videoService.create(request));
    }

    // ADMIN only
    @PutMapping("/{id}")
    public Video updateVideo(@PathVariable Long id, @Valid @RequestBody VideoRequest request) {
        return videoService.update(id, request);
    }

    // ADMIN only
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
        videoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
