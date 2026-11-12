package com.example.rentvideo.service;

import com.example.rentvideo.dto.VideoRequest;
import com.example.rentvideo.entity.Video;
import com.example.rentvideo.exception.ResourceNotFoundException;
import com.example.rentvideo.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VideoServiceTest {

    private VideoRepository videoRepository;
    private VideoService videoService;

    @BeforeEach
    void setUp() {
        videoRepository = mock(VideoRepository.class);
        videoService = new VideoService(videoRepository);
        when(videoRepository.save(any(Video.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void create_defaultsToAvailable() {
        Video v = videoService.create(new VideoRequest("Inception", "Nolan", "Sci-Fi", null));
        assertTrue(v.isAvailable());
        assertEquals("Inception", v.getTitle());
    }

    @Test
    void getAvailableVideos_returnsFromRepository() {
        when(videoRepository.findByAvailableTrue())
                .thenReturn(List.of(new Video("Inception", "Nolan", "Sci-Fi", true)));
        assertEquals(1, videoService.getAvailableVideos().size());
    }

    @Test
    void update_changesFields() {
        Video existing = new Video("Old", "Someone", "Drama", true);
        existing.setId(1L);
        when(videoRepository.findById(1L)).thenReturn(Optional.of(existing));

        Video updated = videoService.update(1L, new VideoRequest("New", "Nolan", "Thriller", false));

        assertEquals("New", updated.getTitle());
        assertEquals("Thriller", updated.getGenre());
        assertFalse(updated.isAvailable());
    }

    @Test
    void getById_missing_throwsNotFound() {
        when(videoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> videoService.getById(99L));
    }

    @Test
    void delete_missing_throwsNotFound() {
        when(videoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> videoService.delete(99L));
        verify(videoRepository, never()).delete(any());
    }
}
