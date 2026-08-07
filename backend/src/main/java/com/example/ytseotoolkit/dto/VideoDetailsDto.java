package com.example.ytseotoolkit.dto;

import java.util.List;

public class VideoDetailsDto {
    private String videoId;
    private String title;
    private String description;
    private List<String> tags;
    private String channelTitle;
    private String channelId;
    private String publishedAt;
    private String categoryId;
    private String duration;
    private String viewCount;
    private String likeCount;
    private String commentCount;
    private ThumbnailSetDto thumbnails;

    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getChannelTitle() { return channelTitle; }
    public void setChannelTitle(String channelTitle) { this.channelTitle = channelTitle; }

    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }

    public String getPublishedAt() { return publishedAt; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getViewCount() { return viewCount; }
    public void setViewCount(String viewCount) { this.viewCount = viewCount; }

    public String getLikeCount() { return likeCount; }
    public void setLikeCount(String likeCount) { this.likeCount = likeCount; }

    public String getCommentCount() { return commentCount; }
    public void setCommentCount(String commentCount) { this.commentCount = commentCount; }

    public ThumbnailSetDto getThumbnails() { return thumbnails; }
    public void setThumbnails(ThumbnailSetDto thumbnails) { this.thumbnails = thumbnails; }
}
