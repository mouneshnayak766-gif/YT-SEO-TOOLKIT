package com.example.ytseotoolkit.controller;

import com.example.ytseotoolkit.dto.ThumbnailSetDto;
import com.example.ytseotoolkit.dto.VideoDetailsDto;
import com.example.ytseotoolkit.exception.YouTubeApiException;
import com.example.ytseotoolkit.service.YouTubeService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/thumbnail")
public class ThumbnailController {

    private final YouTubeService youTubeService;
    private final RestClient rawClient = RestClient.create();

    public ThumbnailController(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    /** GET /api/thumbnail?input=<url-or-id> -> all resolutions */
    @GetMapping
    public ThumbnailSetDto getThumbnails(@RequestParam String input) {
        String videoId = youTubeService.extractVideoId(input);
        VideoDetailsDto details = youTubeService.getVideoDetails(videoId);
        return details.getThumbnails();
    }

    /**
     * GET /api/thumbnail/download?input=<url-or-id>&quality=maxres
     * Proxies the image bytes so the browser can force-download rather than navigate.
     * quality: default | medium | high | standard | maxres
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> download(
            @RequestParam String input,
            @RequestParam(defaultValue = "maxres") String quality) {
        String videoId = youTubeService.extractVideoId(input);
        VideoDetailsDto details = youTubeService.getVideoDetails(videoId);
        ThumbnailSetDto thumbs = details.getThumbnails();

        String url = switch (quality.toLowerCase()) {
            case "default" -> thumbs.getDefaultUrl();
            case "medium" -> thumbs.getMediumUrl();
            case "high" -> thumbs.getHighUrl();
            case "standard" -> thumbs.getStandardUrl();
            default -> thumbs.getMaxResUrl();
        };
        if (url == null) {
            throw new YouTubeApiException("Requested thumbnail quality is not available: " + quality, 404);
        }

        byte[] bytes = rawClient.get().uri(url).retrieve().body(byte[].class);
        String filename = videoId + "_" + quality + ".jpg";

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(new ByteArrayResource(bytes != null ? bytes : new byte[0]).getByteArray());
    }
}
