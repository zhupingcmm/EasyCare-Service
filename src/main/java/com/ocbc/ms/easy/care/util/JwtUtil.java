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

    @Value("${jwt.algorithm:HS256}")
    private String jwtAlgorithm;

    @Value("${jwt.private-key-pem:}")
    private String jwtPrivateKeyPem;

    @Value("${jwt.public-key-pem:}")
    private String jwtPublicKeyPem;

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
                throw new IllegalStateException("JWT私钥PEM未配置 (jwt.private-key-pem)");
            }
            
            String normalized = jwtPrivateKeyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(normalized);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
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
                throw new IllegalStateException("JWT公钥PEM未配置 (jwt.public-key-pem)");
            }
            
            String normalized = jwtPublicKeyPem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(normalized);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            cachedPublicKey = kf.generatePublic(spec);
            return cachedPublicKey;
        }
    }

    /**
     * 生成访问令牌
     */
    public String generateAccessToken(String lanId, String userId, String issuer, String audience) {
        try {
            JWTClaimsSet claimsSet = createClaims(lanId, userId, issuer, audience, "API", accessTokenExpiration);
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader("RS256".equalsIgnoreCase(jwtAlgorithm) ? JWSAlgorithm.RS256 : JWSAlgorithm.HS256),
                    claimsSet
            );

            JWSSigner signer = createSigner();
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            log.error("生成访问令牌时签名失败", e);
            throw new RuntimeException("生成访问令牌失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(String lanId, String userId, String issuer, String audience) {
        try {
            JWTClaimsSet claimsSet = createClaims(lanId, userId, issuer, audience, "REFRESH", refreshTokenExpiration);
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader("RS256".equalsIgnoreCase(jwtAlgorithm) ? JWSAlgorithm.RS256 : JWSAlgorithm.HS256),
                    claimsSet
            );

            JWSSigner signer = createSigner();
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            log.error("生成刷新令牌时签名失败", e);
            throw new RuntimeException("生成刷新令牌失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建JWT Claims
     */
    private JWTClaimsSet createClaims(String lanId, String userId, String issuer, String audience,
                                      String subject, long ttlSeconds) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date exp = new Date(nowMillis + ttlSeconds * 1000);

        return new JWTClaimsSet.Builder()
                .subject(subject)
                .audience(audience)
                .issuer(issuer)
                .issueTime(now)
                .notBeforeTime(now)
                .expirationTime(exp)
                .claim("unique_name", lanId)
                .claim("user_id", userId)
                .build();
    }

    private JWSSigner createSigner() throws Exception {
        if ("RS256".equalsIgnoreCase(jwtAlgorithm)) {
            RSAPrivateKey privateKey = (RSAPrivateKey) getPrivateKey();
            return new RSASSASigner(privateKey);
        } else {
            return new MACSigner(getHmacKey());
        }
    }

    private JWSVerifier createVerifier() throws Exception {
        if ("RS256".equalsIgnoreCase(jwtAlgorithm)) {
            RSAPublicKey publicKey = (RSAPublicKey) getPublicKey();
            return new RSASSAVerifier(publicKey);
        } else {
            return new MACVerifier(getHmacKey());
        }
    }

    private JWTClaimsSet parseAndValidate(String token) throws ParseException, Exception {
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSVerifier verifier = createVerifier();

        if (!signedJWT.verify(verifier)) {
            throw new RuntimeException("JWT签名验证失败");
        }

        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
        Date exp = claims.getExpirationTime();
        if (exp != null && exp.before(new Date())) {
            throw new RuntimeException("JWT已过期");
        }
        return claims;
    }

    /**
     * 验证JWT令牌
     */
    public boolean validateToken(String token) {
        try {
            parseAndValidate(token);
            return true;
        } catch (Exception e) {
            log.error("JWT令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从JWT令牌中提取Claims
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
     * 从JWT令牌中提取用户名
     */
    public String getLanIdFromToken(String token) {
        JWTClaimsSet claims = getClaimsFromToken(token);
        Object value = claims.getClaim("unique_name");
        return value != null ? value.toString() : null;
    }

    /**
     * 从JWT令牌中提取用户ID
     */
    public String getUserIdFromToken(String token) {
        JWTClaimsSet claims = getClaimsFromToken(token);
        Object value = claims.getClaim("user_id");
        return value != null ? value.toString() : null;
    }

    /**
     * 检查JWT令牌是否过期
     */
    public boolean isTokenExpired(String token) {
        JWTClaimsSet claims = getClaimsFromToken(token);
        Date exp = claims.getExpirationTime();
        return exp != null && exp.before(new Date());
    }

    /**
     * 获取访问令牌过期时间
     */
    public LocalDateTime getAccessTokenExpiry() {
        return LocalDateTime.now().plusSeconds(accessTokenExpiration);
    }

    /**
     * 获取刷新令牌过期时间
     */
    public LocalDateTime getRefreshTokenExpiry() {
        return LocalDateTime.now().plusSeconds(refreshTokenExpiration);
    }

    /**
     * 将Date转换为LocalDateTime
     */
    public LocalDateTime dateToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
