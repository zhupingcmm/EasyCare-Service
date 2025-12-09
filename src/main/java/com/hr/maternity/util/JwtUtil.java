package com.hr.maternity.util;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Base64;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret:mySecretKey12345678901234567890123456789012}")
    private String jwtSecret;

    @Value("${jwt.access-token.expiration:600}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration:600}")
    private Long refreshTokenExpiration;

    @Value("${jwt.algorithm:RS256}")
    private String jwtAlgorithm;

    @Value("${jwt.issuer:HR}")
    private String jwtIssuer;

    @Value("${jwt.audience:OCBC}")
    private String jwtAudience;

    @Value("${jwt.private-key-pem:}")
    private String jwtPrivateKeyPem;

    @Value("${jwt.public-key-pem:}")
    private String jwtPublicKeyPem;

    private static final String RSA_KEY_ALGORITHM = "RSA";
    private static final String CLAIM_UNIQUE_NAME = "unique_name";
    private static final String CLAIM_USER_ID = "user_id";
    private static final String TOKEN_TYPE_API = "API";
    private static final String TOKEN_TYPE_REFRESH = "REFRESH";
    private static final int MILLIS_PER_SECOND = 1000;
    private static final int MIN_HMAC_KEY_LENGTH = 32;

    private volatile PrivateKey cachedPrivateKey;
    private volatile PublicKey cachedPublicKey;

    private byte[] getHmacKey() {
        return jwtSecret.getBytes(StandardCharsets.UTF_8);
    }

    private PrivateKey getPrivateKey() throws Exception {
        if (cachedPrivateKey != null) {
            return cachedPrivateKey;
        }
        
        synchronized (this) {
            if (cachedPrivateKey != null) {
                return cachedPrivateKey;
            }
            
            if (jwtPrivateKeyPem == null || jwtPrivateKeyPem.isBlank()) {
                throw new IllegalStateException("JWT private key PEM not configured (jwt.private-key-pem)");
            }
            
            String normalized = jwtPrivateKeyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(normalized);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
            cachedPrivateKey = kf.generatePrivate(spec);
            return cachedPrivateKey;
        }
    }

    private PublicKey getPublicKey() throws Exception {
        if (cachedPublicKey != null) {
            return cachedPublicKey;
        }
        
        synchronized (this) {
            if (cachedPublicKey != null) {
                return cachedPublicKey;
            }
            
            if (jwtPublicKeyPem == null || jwtPublicKeyPem.isBlank()) {
                throw new IllegalStateException("JWT public key PEM not configured (jwt.public-key-pem)");
            }
            
            String normalized = jwtPublicKeyPem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(normalized);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
            cachedPublicKey = kf.generatePublic(spec);
            return cachedPublicKey;
        }
    }

    /**
     * Get JWS Algorithm based on configuration
     */
    private JWSAlgorithm getJWSAlgorithm() {
        String algo = jwtAlgorithm.toUpperCase();
        
        if (algo.startsWith("RS")) {
            switch (algo) {
                case "RS256": return JWSAlgorithm.RS256;
                case "RS384": return JWSAlgorithm.RS384;
                case "RS512": return JWSAlgorithm.RS512;
                default: throw new IllegalArgumentException("Unsupported RSA algorithm: " + jwtAlgorithm);
            }
        } else if (algo.startsWith("HS")) {
            switch (algo) {
                case "HS256": return JWSAlgorithm.HS256;
                case "HS384": return JWSAlgorithm.HS384;
                case "HS512": return JWSAlgorithm.HS512;
                default: throw new IllegalArgumentException("Unsupported HMAC algorithm: " + jwtAlgorithm);
            }
        } else {
            throw new IllegalArgumentException("Unsupported JWT algorithm: " + jwtAlgorithm);
        }
    }

    /**
     * Generate access token
     */
    public String generateAccessToken(String lanId, String userId, String issuer, String audience) {
        try {
            JWTClaimsSet claimsSet = createClaims(lanId, userId, issuer, audience, TOKEN_TYPE_API, accessTokenExpiration);
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(getJWSAlgorithm()),
                    claimsSet
            );

            JWSSigner signer = createSigner();
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            log.error("Failed to sign access token", e);
            throw new RuntimeException("Failed to generate access token: " + e.getMessage(), e);
        }
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(String lanId, String userId, String issuer, String audience) {
        try {
            JWTClaimsSet claimsSet = createClaims(lanId, userId, issuer, audience, TOKEN_TYPE_REFRESH, refreshTokenExpiration);
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(getJWSAlgorithm()),
                    claimsSet
            );

            JWSSigner signer = createSigner();
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            log.error("Failed to sign refresh token", e);
            throw new RuntimeException("Failed to generate refresh token: " + e.getMessage(), e);
        }
    }

    /**
     * Create JWT Claims
     */
    private JWTClaimsSet createClaims(String lanId, String userId, String issuer, String audience,
                                      String subject, long ttlSeconds) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date exp = new Date(nowMillis + ttlSeconds * MILLIS_PER_SECOND);

        return new JWTClaimsSet.Builder()
                .subject(subject)
                .audience(audience)
                .issuer(issuer)
                .issueTime(now)
                .notBeforeTime(now)
                .expirationTime(exp)
                .claim(CLAIM_UNIQUE_NAME, lanId)
                .claim(CLAIM_USER_ID, userId)
                .build();
    }

    /**
     * Create JWT Signer
     */
    private JWSSigner createSigner() throws Exception {
        if (jwtAlgorithm.toUpperCase().startsWith("RS")) {
            if (jwtPrivateKeyPem == null || jwtPrivateKeyPem.isEmpty()) {
                throw new IllegalStateException("JWT private key is required for RSA algorithm");
            }
            RSAPrivateKey privateKey = (RSAPrivateKey) getPrivateKey();
            log.debug("Using RSA signer with algorithm: {}", jwtAlgorithm);
            return new RSASSASigner(privateKey);
        } else if (jwtAlgorithm.toUpperCase().startsWith("HS")) {
            byte[] hmacKey = getHmacKey();
            if (hmacKey.length < MIN_HMAC_KEY_LENGTH) {
                log.warn("HMAC key is too short, recommended at least {} bytes", MIN_HMAC_KEY_LENGTH);
            }
            log.debug("Using HMAC signer with algorithm: {}", jwtAlgorithm);
            return new MACSigner(hmacKey);
        } else {
            throw new IllegalArgumentException("Unsupported JWT algorithm: " + jwtAlgorithm);
        }
    }

    /**
     * Create JWT Verifier
     */
    private JWSVerifier createVerifier() throws Exception {
        if (jwtAlgorithm.toUpperCase().startsWith("RS")) {
            if (jwtPublicKeyPem == null || jwtPublicKeyPem.isEmpty()) {
                throw new IllegalStateException("JWT public key is required for RSA algorithm");
            }
            RSAPublicKey publicKey = (RSAPublicKey) getPublicKey();
            log.debug("Using RSA verifier with algorithm: {}", jwtAlgorithm);
            return new RSASSAVerifier(publicKey);
        } else if (jwtAlgorithm.toUpperCase().startsWith("HS")) {
            log.debug("Using HMAC verifier with algorithm: {}", jwtAlgorithm);
            return new MACVerifier(getHmacKey());
        } else {
            throw new IllegalArgumentException("Unsupported JWT algorithm: " + jwtAlgorithm);
        }
    }

    private JWTClaimsSet parseAndValidate(String token) throws ParseException, Exception {
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSVerifier verifier = createVerifier();

        if (!signedJWT.verify(verifier)) {
            throw new RuntimeException("JWT signature verification failed");
        }

        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
        Date exp = claims.getExpirationTime();
        if (exp != null && exp.before(new Date())) {
            throw new RuntimeException("JWT has expired");
        }
        return claims;
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            parseAndValidate(token);
            return true;
        } catch (Exception e) {
            log.error("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get Claims from JWT token
     */
    public JWTClaimsSet getClaimsFromToken(String token) {
        try {
            return parseAndValidate(token);
        } catch (Exception e) {
            log.error("Failed to parse JWT token: {}", e.getMessage());
            throw new RuntimeException("Failed to parse JWT token: " + e.getMessage(), e);
        }
    }

    /**
     * Get LAN ID from JWT token
     */
    public String getLanIdFromToken(String token) {
        JWTClaimsSet claims = getClaimsFromToken(token);
        Object value = claims.getClaim(CLAIM_UNIQUE_NAME);
        return value != null ? value.toString() : null;
    }

    /**
     * Get User ID from JWT token
     */
    public String getUserIdFromToken(String token) {
        JWTClaimsSet claims = getClaimsFromToken(token);
        Object value = claims.getClaim(CLAIM_USER_ID);
        return value != null ? value.toString() : null;
    }

    /**
     * Check if JWT token is expired
     */
    public boolean isTokenExpired(String token) {
        JWTClaimsSet claims = getClaimsFromToken(token);
        Date exp = claims.getExpirationTime();
        return exp != null && exp.before(new Date());
    }

    /**
     * Get access token expiry time
     */
    public LocalDateTime getAccessTokenExpiry() {
        return LocalDateTime.now().plusSeconds(accessTokenExpiration);
    }

    /**
     * Get refresh token expiry time
     */
    public LocalDateTime getRefreshTokenExpiry() {
        return LocalDateTime.now().plusSeconds(refreshTokenExpiration);
    }

    /**
     * Convert Date to LocalDateTime
     */
    public LocalDateTime dateToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
