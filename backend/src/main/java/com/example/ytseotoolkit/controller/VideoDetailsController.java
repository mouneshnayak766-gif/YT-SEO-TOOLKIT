package com.example.ytseotoolkit.controller;

import com.example.ytseotoolkit.dto.VideoDetailsDto;
import com.example.ytseotoolkit.service.YouTubeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video")
public class VideoDetailsController {

    private final YouTubeService youTubeService;

    public VideoDetailsController(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    /** GET /api/video/details?input=<url-or-id> */
    @GetMapping("/details")
    public VideoDetailsDto getDetails(@RequestParam String input) {
        String videoId = youTubeService.extractVideoId(input);
        return youTubeService.getVideoDetails(videoId);
    }
}
