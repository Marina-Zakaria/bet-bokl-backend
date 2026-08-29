package com.bokl.homerental.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.s3")
public class S3Properties {

    private String region = "eu-north-1";
    private String bucket = "";
    private String publicBaseUrl = "";
    private String accessKeyId = "";
    private String secretAccessKey = "";
    private int presignExpirationSeconds = 900;

    public boolean isConfigured() {
        return bucket != null && !bucket.isBlank();
    }

    public boolean hasStaticCredentials() {
        return accessKeyId != null && !accessKeyId.isBlank()
                && secretAccessKey != null && !secretAccessKey.isBlank();
    }

    public String resolvedPublicBaseUrl() {
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            return publicBaseUrl.replaceAll("/+$", "");
        }
        return "https://" + bucket + ".s3." + region + ".amazonaws.com";
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    public int getPresignExpirationSeconds() {
        return presignExpirationSeconds;
    }

    public void setPresignExpirationSeconds(int presignExpirationSeconds) {
        this.presignExpirationSeconds = presignExpirationSeconds;
    }
}
