package ar.edu.programacion2.catalog.security;

import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public final class JwtSettings {
    private final SecretKey key;
    private final String issuer;
    private final String audience;
    public JwtSettings(String encoded, String issuer, String audience) {
        try {
            byte[] bytes = Base64.getDecoder().decode(encoded);
            if (bytes.length < 32 || issuer.isBlank() || audience.isBlank()) throw new IllegalArgumentException();
            key = new SecretKeySpec(bytes,"HmacSHA256");
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("Invalid catalog JWT configuration: require Base64 key of at least 32 bytes, issuer and audience");
        }
        this.issuer = issuer;
        this.audience = audience;
    }
    public SecretKey key() { return key; }
    public String issuer() { return issuer; }
    public String audience() { return audience; }
}
