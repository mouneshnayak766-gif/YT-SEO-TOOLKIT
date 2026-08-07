package com.example.ytseotoolkit.dto;

public class ThumbnailSetDto {
    private String defaultUrl;
    private String mediumUrl;
    private String highUrl;
    private String standardUrl;
    private String maxResUrl;

    public String getDefaultUrl() { return defaultUrl; }
    public void setDefaultUrl(String defaultUrl) { this.defaultUrl = defaultUrl; }

    public String getMediumUrl() { return mediumUrl; }
    public void setMediumUrl(String mediumUrl) { this.mediumUrl = mediumUrl; }

    public String getHighUrl() { return highUrl; }
    public void setHighUrl(String highUrl) { this.highUrl = highUrl; }

    public String getStandardUrl() { return standardUrl; }
    public void setStandardUrl(String standardUrl) { this.standardUrl = standardUrl; }

    public String getMaxResUrl() { return maxResUrl; }
    public void setMaxResUrl(String maxResUrl) { this.maxResUrl = maxResUrl; }
}
