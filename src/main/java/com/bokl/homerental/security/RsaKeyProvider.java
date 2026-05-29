package com.bokl.homerental.security;

import com.bokl.homerental.service.MessageService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Manages the RSA key pair used for field-level encryption.
 *
 * <p>If {@code rsa.private-key} and {@code rsa.public-key} are set (Base64-encoded DER),
 * those keys are loaded. Otherwise a 2048-bit key pair is generated on startup
 * (ephemeral — clients must re-fetch the public key after each restart).
 *
 * <p>Encryption scheme: RSA-OAEP with SHA-256 hash and SHA-256 MGF1.
 */
@Component
public class RsaKeyProvider {

    private static final Logger log = LoggerFactory.getLogger(RsaKeyProvider.class);

    @Value("${rsa.private-key:}")
    private String configuredPrivateKey;

    @Value("${rsa.public-key:}")
    private String configuredPublicKey;

    private PrivateKey privateKey;
    private PublicKey  publicKey;
    private final MessageService msg;

    public RsaKeyProvider(MessageService msg) {
        this.msg = msg;
    }

    @PostConstruct
    public void init() throws Exception {
        if (!configuredPrivateKey.isBlank() && !configuredPublicKey.isBlank()) {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            this.privateKey = kf.generatePrivate(
                    new PKCS8EncodedKeySpec(Base64.getDecoder().decode(configuredPrivateKey)));
            this.publicKey  = kf.generatePublic(
                    new X509EncodedKeySpec(Base64.getDecoder().decode(configuredPublicKey)));
            log.info("RSA key pair loaded from configuration.");
        } else {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair kp = gen.generateKeyPair();
            this.privateKey = kp.getPrivate();
            this.publicKey  = kp.getPublic();
            log.info("RSA key pair generated on startup (ephemeral). " +
                     "Set rsa.private-key and rsa.public-key env vars for persistence.");
        }
    }

    /** Returns the DER-encoded SubjectPublicKeyInfo, Base64-encoded (usable by Postman/clients). */
    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Decrypts a Base64-encoded RSA-OAEP (SHA-256 / SHA-256 MGF1) ciphertext.
     *
     * @throws ResponseStatusException HTTP 400 if decryption fails (wrong key or tampered data)
     */
    public String decrypt(String base64Ciphertext) {
        try {
            byte[] cipherBytes = Base64.getDecoder().decode(base64Ciphertext);
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
            OAEPParameterSpec spec = new OAEPParameterSpec(
                    "SHA-256", "MGF1",
                    new MGF1ParameterSpec("SHA-256"),
                    PSource.PSpecified.DEFAULT);
            cipher.init(Cipher.DECRYPT_MODE, privateKey, spec);
            return new String(cipher.doFinal(cipherBytes), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.debug("RSA decryption failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    msg.get("auth.error.invalid_encrypted_field_value"));
        }
    }
}
