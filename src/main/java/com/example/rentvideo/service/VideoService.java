package com.example.rentvideo.service;

import com.example.rentvideo.dto.VideoRequest;
import com.example.rentvideo.entity.Video;
import com.example.rentvideo.exception.ResourceNotFoundException;
import com.example.rentvideo.repository.VideoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VideoService {

    private final VideoRepository videoRepository;

    public VideoService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    public List<Video> getAvailableVideos() {
        return videoRepository.findByAvailableTrue();
    }

    public Video getById(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id " + id));
    }

    public Video create(VideoRequest req) {
        Video video = new Video(req.title(), req.director(), req.genre(),
                req.available() == null || req.available());
        return videoRepository.save(video);
    }

    public Video update(Long id, VideoRequest req) {
        Video video = getById(id);
        video.setTitle(req.title());
        video.setDirector(req.director());
        video.setGenre(req.genre());
        if (req.available() != null) {
            video.setAvailable(req.available());
        }
        return videoRepository.save(video);
    }

    public void delete(Long id) {
        Video video = getById(id);
        videoRepository.delete(video);
    }
}
