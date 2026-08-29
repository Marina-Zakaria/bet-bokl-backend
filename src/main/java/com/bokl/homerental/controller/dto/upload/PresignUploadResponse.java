package com.bokl.homerental.controller.dto.upload;

import java.util.Map;

public class PresignUploadResponse {

    private String id;
    private String url;
    private String uploadUrl;
    private Map<String, String> headers;
    private int expiresInSeconds;

    public PresignUploadResponse() {}

    public PresignUploadResponse(
            String id,
            String url,
            String uploadUrl,
            Map<String, String> headers,
            int expiresInSeconds) {
        this.id = id;
        this.url = url;
        this.uploadUrl = uploadUrl;
        this.headers = headers;
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public int getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(int expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }
}
