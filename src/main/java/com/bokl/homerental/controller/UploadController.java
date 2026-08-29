package com.bokl.homerental.controller;

import com.bokl.homerental.annotation.RequiresLogin;
import com.bokl.homerental.controller.dto.upload.PresignUploadRequest;
import com.bokl.homerental.controller.dto.upload.PresignUploadResponse;
import com.bokl.homerental.service.storage.S3PresignService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final S3PresignService presignService;

    public UploadController(S3PresignService presignService) {
        this.presignService = presignService;
    }

    /**
     * Returns a presigned PUT URL so the client can upload bytes directly to S3.
     * The returned {@code url} is the public object URL to store in API payloads.
     */
    @RequiresLogin
    @PostMapping("/presign")
    public PresignUploadResponse presign(@Valid @RequestBody PresignUploadRequest request) {
        return presignService.presign(request);
    }
}
