package com.example.ytseotoolkit.exception;

public class YouTubeApiException extends RuntimeException {
    private final int statusCode;

    public YouTubeApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public YouTubeApiException(String message) {
        super(message);
        this.statusCode = 500;
    }

    public int getStatusCode() { return statusCode; }
}
