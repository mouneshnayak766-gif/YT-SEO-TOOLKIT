package com.example.ytseotoolkit.controller;

import com.example.ytseotoolkit.dto.SeoTagResultDto;
import com.example.ytseotoolkit.service.YouTubeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seo-tags")
public class SeoTagController {

    private final YouTubeService youTubeService;

    public SeoTagController(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    /** GET /api/seo-tags?title=...&relatedCount=5 */
    @GetMapping
    public SeoTagResultDto generateTags(
            @RequestParam String title,
            @RequestParam(defaultValue = "5") int relatedCount) {
        return youTubeService.generateSeoTags(title, relatedCount);
    }

    /** GET /api/seo-tags/keywords?seed=spring+boot&maxResults=10 */
    @GetMapping("/keywords")
    public Map<String, Object> trendingKeywords(
            @RequestParam String seed,
            @RequestParam(defaultValue = "10") int maxResults) {
        List<String> suggestions = youTubeService.suggestKeywords(seed, maxResults);
        return Map.of("seed", seed, "suggestions", suggestions);
    }
}
