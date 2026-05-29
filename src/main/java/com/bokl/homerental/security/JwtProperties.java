package com.bokl.homerental.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** Base64-encoded HMAC-SHA256 secret — must decode to at least 32 bytes. */
    private String secret;

    /** Prefix used in the Authorization header value (e.g. "Bearer"). */
    private String tokenType = "Bearer";

    public String getSecret()                    { return secret; }
    public void setSecret(String secret)         { this.secret = secret; }
    public String getTokenType()                 { return tokenType; }
    public void setTokenType(String tokenType)   { this.tokenType = tokenType; }
}
