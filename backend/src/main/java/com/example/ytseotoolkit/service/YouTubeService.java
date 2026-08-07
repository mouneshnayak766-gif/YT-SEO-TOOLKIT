package com.example.ytseotoolkit.service;

import com.example.ytseotoolkit.dto.*;
import com.example.ytseotoolkit.exception.YouTubeApiException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class YouTubeService {

    private final RestClient restClient;
    private final String apiKey;

    private static final Pattern[] VIDEO_ID_PATTERNS = new Pattern[]{
            Pattern.compile("(?:v=|/videos/|embed/|youtu\\.be/|/shorts/)([a-zA-Z0-9_-]{11})"),
            Pattern.compile("^([a-zA-Z0-9_-]{11})$")
    };

    public YouTubeService(@Value("${youtube.api.base-url}") String baseUrl,
                           @Value("${youtube.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /** Extracts an 11-char YouTube video ID from a full URL, short URL, or raw ID. */
    public String extractVideoId(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Video URL or ID must not be empty");
        }
        String trimmed = input.trim();
        for (Pattern p : VIDEO_ID_PATTERNS) {
            Matcher m = p.matcher(trimmed);
            if (m.find()) {
                return m.group(1);
            }
        }
        throw new IllegalArgumentException("Could not extract a valid YouTube video ID from: " + input);
    }

    private void assertApiKeyConfigured() {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("PUT_YOUR_API_KEY_HERE")) {
            throw new YouTubeApiException("YouTube API key is not configured on the server. Set YOUTUBE_API_KEY.", 500);
        }
    }

    private JsonNode get(String path, Map<String, String> params) {
        assertApiKeyConfigured();
        StringBuilder query = new StringBuilder(path).append("?key=").append(apiKey);
        for (Map.Entry<String, String> e : params.entrySet()) {
            query.append("&").append(e.getKey()).append("=")
                    .append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
        }
        try {
            return restClient.get().uri(query.toString()).retrieve().body(JsonNode.class);
        } catch (HttpClientErrorException ex) {
            String msg = "YouTube API request failed: " + ex.getStatusCode();
            try {
                JsonNode errBody = ex.getResponseBodyAs(JsonNode.class);
                if (errBody != null && errBody.has("error")) {
                    JsonNode err = errBody.get("error");
                    if (err.has("message")) msg = err.get("message").asText();
                }
            } catch (Exception ignored) { }
            throw new YouTubeApiException(msg, ex.getStatusCode().value());
        }
    }

    /** Fetch full details for a single video by ID. */
    public VideoDetailsDto getVideoDetails(String videoId) {
        JsonNode root = get("/videos", Map.of(
                "part", "snippet,contentDetails,statistics",
                "id", videoId
        ));
        JsonNode items = root.path("items");
        if (!items.isArray() || items.isEmpty()) {
            throw new YouTubeApiException("No video found for ID: " + videoId, 404);
        }
        return mapVideoNode(items.get(0));
    }

    private VideoDetailsDto mapVideoNode(JsonNode item) {
        JsonNode snippet = item.path("snippet");
        JsonNode statistics = item.path("statistics");
        JsonNode contentDetails = item.path("contentDetails");
        JsonNode thumbs = snippet.path("thumbnails");

        VideoDetailsDto dto = new VideoDetailsDto();
        dto.setVideoId(item.path("id").asText());
        dto.setTitle(snippet.path("title").asText(null));
        dto.setDescription(snippet.path("description").asText(null));
        dto.setChannelTitle(snippet.path("channelTitle").asText(null));
        dto.setChannelId(snippet.path("channelId").asText(null));
        dto.setPublishedAt(snippet.path("publishedAt").asText(null));
        dto.setCategoryId(snippet.path("categoryId").asText(null));
        dto.setDuration(contentDetails.path("duration").asText(null));
        dto.setViewCount(statistics.path("viewCount").asText(null));
        dto.setLikeCount(statistics.path("likeCount").asText(null));
        dto.setCommentCount(statistics.path("commentCount").asText(null));

        List<String> tags = new ArrayList<>();
        if (snippet.has("tags") && snippet.path("tags").isArray()) {
            snippet.path("tags").forEach(t -> tags.add(t.asText()));
        }
        dto.setTags(tags);

        ThumbnailSetDto thumbSet = new ThumbnailSetDto();
        thumbSet.setDefaultUrl(textOrNull(thumbs, "default"));
        thumbSet.setMediumUrl(textOrNull(thumbs, "medium"));
        thumbSet.setHighUrl(textOrNull(thumbs, "high"));
        thumbSet.setStandardUrl(textOrNull(thumbs, "standard"));
        thumbSet.setMaxResUrl(textOrNull(thumbs, "maxres"));
        // Fallback: maxres isn't always present via API even if it exists on i.ytimg.com
        if (thumbSet.getMaxResUrl() == null) {
            thumbSet.setMaxResUrl("https://i.ytimg.com/vi/" + dto.getVideoId() + "/maxresdefault.jpg");
        }
        dto.setThumbnails(thumbSet);
        return dto;
    }

    private String textOrNull(JsonNode thumbs, String key) {
        JsonNode node = thumbs.path(key).path("url");
        return node.isMissingNode() ? null : node.asText();
    }

    /** Search videos by a free-text title/query. Returns lightweight results. */
    public List<VideoSearchHit> searchByTitle(String query, int maxResults) {
        JsonNode root = get("/search", Map.of(
                "part", "snippet",
                "q", query,
                "type", "video",
                "maxResults", String.valueOf(Math.max(1, Math.min(maxResults, 25)))
        ));
        List<VideoSearchHit> hits = new ArrayList<>();
        JsonNode items = root.path("items");
        if (items.isArray()) {
            for (JsonNode item : items) {
                String vid = item.path("id").path("videoId").asText(null);
                if (vid == null) continue;
                JsonNode snippet = item.path("snippet");
                hits.add(new VideoSearchHit(vid, snippet.path("title").asText(null),
                        snippet.path("channelTitle").asText(null)));
            }
        }
        return hits;
    }

    /**
     * Core SEO tag generator: searches by title, pulls the primary (top) result's tags,
     * then pulls tags from the next N related results (since the API's relatedToVideoId
     * parameter was deprecated, "related" = next-best search matches for the same query).
     */
    public SeoTagResultDto generateSeoTags(String titleQuery, int relatedCount) {
        List<VideoSearchHit> hits = searchByTitle(titleQuery, Math.max(1, relatedCount + 1));
        if (hits.isEmpty()) {
            throw new YouTubeApiException("No videos found for query: " + titleQuery, 404);
        }

        SeoTagResultDto result = new SeoTagResultDto();
        result.setQueryTitle(titleQuery);

        VideoSearchHit primaryHit = hits.get(0);
        VideoDetailsDto primary = getVideoDetails(primaryHit.videoId());
        result.setPrimaryVideoId(primary.getVideoId());
        result.setPrimaryVideoTitle(primary.getTitle());
        result.setPrimaryTags(primary.getTags());

        LinkedHashSet<String> combined = new LinkedHashSet<>();
        if (primary.getTags() != null) combined.addAll(primary.getTags());

        List<RelatedVideoTagsDto> related = new ArrayList<>();
        for (int i = 1; i < hits.size(); i++) {
            VideoSearchHit hit = hits.get(i);
            try {
                VideoDetailsDto details = getVideoDetails(hit.videoId());
                RelatedVideoTagsDto r = new RelatedVideoTagsDto();
                r.setVideoId(details.getVideoId());
                r.setTitle(details.getTitle());
                r.setChannelTitle(details.getChannelTitle());
                r.setTags(details.getTags());
                related.add(r);
                if (details.getTags() != null) combined.addAll(details.getTags());
            } catch (YouTubeApiException ignored) {
                // skip a bad related video rather than failing the whole request
            }
        }
        result.setRelatedVideos(related);
        result.setCombinedUniqueTags(new ArrayList<>(combined));
        return result;
    }

    /** Lightweight keyword/title suggestions for a seed query, used for "trending keywords". */
    public List<String> suggestKeywords(String seed, int maxResults) {
        List<VideoSearchHit> hits = searchByTitle(seed, maxResults);
        return hits.stream()
                .map(VideoSearchHit::title)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    public record VideoSearchHit(String videoId, String title, String channelTitle) { }
}
