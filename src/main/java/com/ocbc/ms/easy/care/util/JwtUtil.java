package com.ocbc.ms.easy.care.util;

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
import com.ocbc.ms.easy.care.config.JwtConfigurationProperties;
import com.ocbc.ms.easy.care.encryption.config.EncryptionProperties;
import com.ocbc.ms.easy.care.entity.Token;
import com.ocbc.ms.easy.care.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final TokenRepository tokenRepository;
    private final EncryptionProperties encryptionProperties;
    private final JwtConfigurationProperties jwtConfig;

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
        return jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8);
    }

    private boolean isRsaAlgorithm() {
        return jwtConfig.getAlgorithm().toUpperCase().startsWith("RS");
    }

    private boolean isHmacAlgorithm() {
        return jwtConfig.getAlgorithm().toUpperCase().startsWith("HS");
    }

    private PrivateKey getPrivateKey() throws Exception {
        if (cachedPrivateKey != null) {
            return cachedPrivateKey;
        }
        
        synchronized (this) {
            if (cachedPrivateKey != null) {
                return cachedPrivateKey;
            }
            
            String privateKeyPem = encryptionProperties.getRsaPrivateKeyPem();
            if (privateKeyPem == null || privateKeyPem.isBlank()) {
                throw new IllegalStateException("RSA private key PEM not configured (encryption.rsa-private-key-pem)");
            }
            
            String normalized = privateKeyPem
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
            
            String publicKeyPem = encryptionProperties.getRsaPublicKeyPem();
            if (publicKeyPem == null || publicKeyPem.isBlank()) {
                throw new IllegalStateException("RSA public key PEM not configured (encryption.rsa-public-key-pem)");
            }
            
            String normalized = publicKeyPem
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
        String algo = jwtConfig.getAlgorithm().toUpperCase();
        
        return switch (algo) {
            case "RS256" -> JWSAlgorithm.RS256;
            case "RS384" -> JWSAlgorithm.RS384;
            case "RS512" -> JWSAlgorithm.RS512;
            case "HS256" -> JWSAlgorithm.HS256;
            case "HS384" -> JWSAlgorithm.HS384;
            case "HS512" -> JWSAlgorithm.HS512;
            default -> throw new IllegalArgumentException("Unsupported JWT algorithm: " + algo);
        };
    }

    /**
     * Generate access token
     */
    public String generateAccessToken(String lanId, String userId, String issuer, String audience) {
        try {
            JWTClaimsSet claimsSet = createClaims(lanId, userId, issuer, audience, 
                    TOKEN_TYPE_API, jwtConfig.getAccessToken().getExpiration());
            return signToken(claimsSet);
        } catch (Exception e) {
            log.error("生成访问令牌失败", e);
            throw new RuntimeException("生成访问令牌失败: " + e.getMessage(), e);
        }
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(String lanId, String userId, String issuer, String audience) {
        try {
            JWTClaimsSet claimsSet = createClaims(lanId, userId, issuer, audience, 
                    TOKEN_TYPE_REFRESH, jwtConfig.getRefreshToken().getExpiration());
            return signToken(claimsSet);
        } catch (Exception e) {
            log.error("生成刷新令牌失败", e);
            throw new RuntimeException("生成刷新令牌失败: " + e.getMessage(), e);
        }
    }

    private String signToken(JWTClaimsSet claimsSet) throws Exception {
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(getJWSAlgorithm()), claimsSet);
        JWSSigner signer = createSigner();
        signedJWT.sign(signer);
        return signedJWT.serialize();
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
        if (isRsaAlgorithm()) {
            RSAPrivateKey privateKey = (RSAPrivateKey) getPrivateKey();
            return new RSASSASigner(privateKey);
        } else if (isHmacAlgorithm()) {
            byte[] hmacKey = getHmacKey();
            if (hmacKey.length < MIN_HMAC_KEY_LENGTH) {
                log.warn("HMAC密钥长度不足，建议至少 {} 字节", MIN_HMAC_KEY_LENGTH);
            }
            return new MACSigner(hmacKey);
        }
        throw new IllegalArgumentException("不支持的JWT算法: " + jwtConfig.getAlgorithm());
    }

    /**
     * Create JWT Verifier
     */
    private JWSVerifier createVerifier() throws Exception {
        if (isRsaAlgorithm()) {
            RSAPublicKey publicKey = (RSAPublicKey) getPublicKey();
            log.debug("使用RSA验证器，算法: {}", jwtConfig.getAlgorithm());
            return new RSASSAVerifier(publicKey);
        } else if (isHmacAlgorithm()) {
            log.debug("使用HMAC验证器，算法: {}", jwtConfig.getAlgorithm());
            return new MACVerifier(getHmacKey());
        }
        throw new IllegalArgumentException("不支持的JWT算法: " + jwtConfig.getAlgorithm());
    }

    private JWTClaimsSet parseAndValidate(String token) throws Exception {
        String realToken = getRealToken(token);
        SignedJWT signedJWT = SignedJWT.parse(realToken);
        
        JWSVerifier verifier = createVerifier();
        if (!signedJWT.verify(verifier)) {
            throw new RuntimeException("JWT签名验证失败");
        }

        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
        validateTokenExpiration(claims);
        return claims;
    }

    private String getRealToken(String token) {
        return tokenRepository.findByOpAccTokenAndRevokedFalse(token)
                .map(Token::getAccToken)
                .orElseGet(() -> tokenRepository.findByOpRefTokenAndRevokedFalse(token)
                        .map(Token::getRefToken)
                        .orElse(token));
    }

    private void validateTokenExpiration(JWTClaimsSet claims) {
        Date exp = claims.getExpirationTime();
        if (exp != null && exp.before(new Date())) {
            throw new RuntimeException("JWT令牌已过期");
        }
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            parseAndValidate(token);
            return true;
        } catch (Exception e) {
            log.debug("JWT令牌验证失败: {}", e.getMessage());
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
            log.error("解析JWT令牌失败: {}", e.getMessage());
            throw new RuntimeException("解析JWT令牌失败: " + e.getMessage(), e);
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
        return LocalDateTime.now().plusSeconds(jwtConfig.getAccessToken().getExpiration());
    }

    /**
     * Get refresh token expiry time
     */
    public LocalDateTime getRefreshTokenExpiry() {
        return LocalDateTime.now().plusSeconds(jwtConfig.getRefreshToken().getExpiration());
    }

    /**
     * Convert Date to LocalDateTime
     */
    public LocalDateTime dateToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
