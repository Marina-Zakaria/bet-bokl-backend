package com.bokl.homerental.controller.dto.upload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PresignUploadRequest {

    @NotBlank
    @Size(max = 64)
    private String purpose;

    @NotBlank
    @Size(max = 255)
    private String fileName;

    @NotBlank
    @Size(max = 128)
    private String contentType;

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
