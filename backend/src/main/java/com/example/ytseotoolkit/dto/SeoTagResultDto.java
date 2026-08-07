package com.example.ytseotoolkit.dto;

import java.util.List;

public class SeoTagResultDto {
    private String queryTitle;
    private String primaryVideoId;
    private String primaryVideoTitle;
    private List<String> primaryTags;
    private List<RelatedVideoTagsDto> relatedVideos;
    private List<String> combinedUniqueTags;

    public String getQueryTitle() { return queryTitle; }
    public void setQueryTitle(String queryTitle) { this.queryTitle = queryTitle; }

    public String getPrimaryVideoId() { return primaryVideoId; }
    public void setPrimaryVideoId(String primaryVideoId) { this.primaryVideoId = primaryVideoId; }

    public String getPrimaryVideoTitle() { return primaryVideoTitle; }
    public void setPrimaryVideoTitle(String primaryVideoTitle) { this.primaryVideoTitle = primaryVideoTitle; }

    public List<String> getPrimaryTags() { return primaryTags; }
    public void setPrimaryTags(List<String> primaryTags) { this.primaryTags = primaryTags; }

    public List<RelatedVideoTagsDto> getRelatedVideos() { return relatedVideos; }
    public void setRelatedVideos(List<RelatedVideoTagsDto> relatedVideos) { this.relatedVideos = relatedVideos; }

    public List<String> getCombinedUniqueTags() { return combinedUniqueTags; }
    public void setCombinedUniqueTags(List<String> combinedUniqueTags) { this.combinedUniqueTags = combinedUniqueTags; }
}
