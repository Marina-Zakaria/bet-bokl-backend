package com.bokl.homerental.service.storage;

import com.bokl.homerental.config.S3Properties;
import com.bokl.homerental.controller.dto.upload.PresignUploadRequest;
import com.bokl.homerental.controller.dto.upload.PresignUploadResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;

@Service
public class S3PresignService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "image/heic",
            "application/pdf"
    );

    private final S3Presigner presigner;
    private final S3Properties props;

    public S3PresignService(S3Presigner presigner, S3Properties props) {
        this.presigner = presigner;
        this.props = props;
    }

    public PresignUploadResponse presign(PresignUploadRequest request) {
        if (!props.isConfigured()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "S3 upload is not configured");
        }

        String contentType = normalizeContentType(request.getContentType(), request.getFileName());
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Unsupported content type: " + contentType);
        }

        String key = buildObjectKey(request.getPurpose(), request.getFileName());

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(props.getBucket())
                .key(key)
                .contentType(contentType)
                .build();

        PresignedPutObjectRequest presigned = presigner.presignPutObject(builder -> builder
                .signatureDuration(Duration.ofSeconds(props.getPresignExpirationSeconds()))
                .putObjectRequest(putRequest));

        String publicUrl = props.resolvedPublicBaseUrl() + "/" + key;

        return new PresignUploadResponse(
                key,
                publicUrl,
                presigned.url().toString(),
                Map.of("Content-Type", contentType),
                props.getPresignExpirationSeconds());
    }

    static String folderForPurpose(String purpose) {
        if (purpose == null) {
            return "units/media";
        }
        return switch (purpose) {
            case "unit_photo", "photos", "listing_photo", "property_photo" -> "units/photos";
            case "id_front", "id_back", "identity", "id_document",
                 "id_document_front", "id_document_back", "ownership_document" -> "units/identity";
            default -> "units/media";
        };
    }

    static String buildObjectKey(String purpose, String fileName) {
        String folder = folderForPurpose(purpose);
        String safeName = fileName != null ? fileName.split("\\?")[0] : "upload";
        String ext = extensionFor(safeName);
        String digest = sha1Hex(safeName).substring(0, 10);
        return folder + "/" + System.currentTimeMillis() + "_" + digest + ext;
    }

    static String extensionFor(String fileName) {
        int dot = fileName.lastIndexOf('.');
        String ext = dot >= 0 ? fileName.substring(dot).toLowerCase() : "";
        if (ext.equals(".png") || ext.equals(".jpg") || ext.equals(".jpeg")
                || ext.equals(".webp") || ext.equals(".gif") || ext.equals(".heic")
                || ext.equals(".pdf")) {
            return ".jpeg".equals(ext) ? ".jpg" : ext;
        }
        return ".jpg";
    }

    static String normalizeContentType(String contentType, String fileName) {
        if (contentType != null && !contentType.isBlank()) {
            return contentType.split(";")[0].trim().toLowerCase();
        }
        return switch (extensionFor(fileName)) {
            case ".png" -> "image/png";
            case ".webp" -> "image/webp";
            case ".gif" -> "image/gif";
            case ".pdf" -> "application/pdf";
            case ".heic" -> "image/heic";
            default -> "image/jpeg";
        };
    }

    private static String sha1Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 not available", e);
        }
    }
}
