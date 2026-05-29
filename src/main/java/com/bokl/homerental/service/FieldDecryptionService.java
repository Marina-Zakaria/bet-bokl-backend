package com.bokl.homerental.service;

import com.bokl.homerental.security.RsaKeyProvider;
import org.springframework.stereotype.Service;

/**
 * Decrypts field values that were encrypted client-side before being sent to the API.
 *
 * <p>Convention: encrypted values are prefixed with {@code ENC:} followed by the
 * Base64-encoded RSA-OAEP (SHA-256) ciphertext. Plain-text values (no prefix) are
 * returned unchanged for backward compatibility with curl-based tooling and tests.
 *
 * <p>Encrypted fields: {@code password}, {@code newPassword}, {@code otp}.
 */
@Service
public class FieldDecryptionService {

    static final String ENC_PREFIX = "ENC:";

    private final RsaKeyProvider rsaKeyProvider;

    public FieldDecryptionService(RsaKeyProvider rsaKeyProvider) {
        this.rsaKeyProvider = rsaKeyProvider;
    }

    /**
     * If {@code value} starts with {@code ENC:} it is treated as a Base64-encoded
     * RSA-OAEP ciphertext and decrypted using the server's private key.
     * Otherwise the value is returned as-is.
     */
    public String decryptIfEncrypted(String value) {
        if (value == null) return null;
        if (value.startsWith(ENC_PREFIX)) {
            return rsaKeyProvider.decrypt(value.substring(ENC_PREFIX.length()));
        }
        return value;
    }
}
