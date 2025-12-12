package com.ocbc.ms.easy.care.ldap;

import com.ocbc.ms.easy.care.config.LdapConfigurationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.CommunicationException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationNotSupportedException;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static com.ocbc.ms.easy.care.ldap.LdapConstants.Attributes.*;
import static com.ocbc.ms.easy.care.ldap.LdapConstants.Filter.USER_SEARCH_TEMPLATE;
import static org.springframework.ldap.query.LdapQueryBuilder.query;
import org.springframework.ldap.query.SearchScope;

@Slf4j
@Service
@RequiredArgsConstructor
public class LdapAuthService {

    private final LdapConfigurationProperties ldapProps;

    public LdapAuthResult authenticate(String username, String password) {
        long startTime = System.currentTimeMillis();
        log.info("========== LDAP Authentication Started for user: {} ==========", username);
        log.debug("Total configured LDAP domains: {}", ldapProps.getLdap() != null ? ldapProps.getLdap().size() : 0);
        
        LdapAuthResult validationResult = validateCredentials(username, password);
        if (validationResult != null) {
            log.warn("LDAP authentication validation failed for user {}: {}", username, validationResult.getErrorMessage());
            return validationResult;
        }

        int attemptCount = 0;
        for (LdapConfigurationProperties.LdapDomainConfig domainConfig : ldapProps.getLdap()) {
            attemptCount++;
            log.info("[Attempt {}/{}] Trying domain: {}", attemptCount, ldapProps.getLdap().size(), domainConfig.getDomain());
            
            LdapAuthResult result = authenticateWithDomain(username, password, domainConfig);
            if (result.isSuccess()) {
                long duration = System.currentTimeMillis() - startTime;
                log.info("========== LDAP Authentication SUCCESS for user: {} in {}ms ==========", username, duration);
                return result;
            }
            log.debug("[Attempt {}/{}] Failed with domain {}: {}", attemptCount, ldapProps.getLdap().size(), 
                    domainConfig.getDomain(), result.getErrorMessage());
        }

        long duration = System.currentTimeMillis() - startTime;
        log.warn("========== LDAP Authentication FAILED for user {} after {} attempts in {}ms ==========", 
                username, attemptCount, duration);
        return LdapAuthResult.failure("Authentication failed for all configured domains");
    }

    /**
     * 验证用户名和密码
     */
    private LdapAuthResult validateCredentials(String username, String password) {
        if (username == null || username.isBlank()) {
            log.warn("LDAP username is empty");
            return LdapAuthResult.failure("Username cannot be empty");
        }
        if (password == null || password.isBlank()) {
            log.warn("LDAP password is empty");
            return LdapAuthResult.failure("Password cannot be empty");
        }
        if (ldapProps.getLdap() == null || ldapProps.getLdap().isEmpty()) {
            log.warn("No LDAP domains configured");
            return LdapAuthResult.failure("No LDAP domains configured");
        }
        return null;
    }

    /**
     * 使用指定域进行认证
     */
    private LdapAuthResult authenticateWithDomain(String username, String password,
                                                   LdapConfigurationProperties.LdapDomainConfig domainConfig) {
        long domainStartTime = System.currentTimeMillis();
        String domain = domainConfig.getDomain().trim();
        String serverAddr = domainConfig.getLdapServer().trim();
        int port = domainConfig.getLdapPort();
        String baseDn = domainConfig.getBaseDn().trim();
        String dnsName = makeDnsFromBaseDn(baseDn);

        log.info("┌─ Domain Authentication Details ─────────────────────────────");
        log.info("│ User: {}", username);
        log.info("│ Domain: {}", domain);
        log.info("│ Server: {}:{}", serverAddr, port);
        log.info("│ SSL Enabled: {}", domainConfig.isUseSsl());
        log.info("│ Trust All Certificates: {}", domainConfig.isTrustAllCertificates());
        log.info("│ Base DN: {}", baseDn);
        log.info("│ DNS Name: {}", dnsName);
        log.info("│ Connect Timeout: {}ms", domainConfig.getConnectTimeout());
        log.info("│ Read Timeout: {}ms", domainConfig.getReadTimeout());
        log.info("└──────────────────────────────────────────────────────────────");

        try {
            long buildContextStart = System.currentTimeMillis();
            LdapContextSource contextSource = buildContextSource(domainConfig);
            log.debug("Context source built in {}ms", System.currentTimeMillis() - buildContextStart);
            
            long bindStart = System.currentTimeMillis();
            LdapAuthResult bindResult = attemptBind(username, password, domain, dnsName, contextSource);
            log.debug("Bind attempt completed in {}ms", System.currentTimeMillis() - bindStart);

            if (!bindResult.isSuccess()) {
                long domainDuration = System.currentTimeMillis() - domainStartTime;
                log.info("Domain {} authentication failed in {}ms: {}", domain, domainDuration, bindResult.getErrorMessage());
                return bindResult;
            }

            long searchStart = System.currentTimeMillis();
            LdapAuthResult searchResult = searchUser(username, domain, dnsName, baseDn, serverAddr, contextSource);
            log.debug("User search completed in {}ms", System.currentTimeMillis() - searchStart);
            
            long domainDuration = System.currentTimeMillis() - domainStartTime;
            if (searchResult.isSuccess()) {
                log.info("✓ Domain {} authentication successful in {}ms", domain, domainDuration);
            } else {
                log.info("✗ Domain {} authentication failed in {}ms: {}", domain, domainDuration, searchResult.getErrorMessage());
            }
            
            return searchResult;
        } catch (Exception e) {
            long domainDuration = System.currentTimeMillis() - domainStartTime;
            log.error("Unexpected error during LDAP authentication for user {} in domain {} after {}ms", 
                    username, domain, domainDuration, e);
            log.error("Exception type: {}, Message: {}", e.getClass().getSimpleName(), e.getMessage());
            return LdapAuthResult.failure("Authentication error: " + e.getMessage());
        }
    }

    /**
     * 尝试绑定LDAP
     */
    private LdapAuthResult attemptBind(String username, String password, String domain,
                                       String dnsName, LdapContextSource contextSource) {
        List<String> principals = buildPrincipals(username, domain, dnsName);
        log.info("Starting bind attempts with {} principal formats", principals.size());
        log.debug("Principal formats to try: {}", principals);
        
        LdapAuthResult lastError = null;
        int attemptNumber = 0;

        for (String principal : principals) {
            attemptNumber++;
            long bindStartTime = System.currentTimeMillis();
            try {
                log.info("  [{}/{}] Binding as: {}", attemptNumber, principals.size(), principal);
                contextSource.setUserDn(principal);
                contextSource.setPassword(password);
                contextSource.afterPropertiesSet();
                contextSource.getContext(principal, password);
                
                long bindDuration = System.currentTimeMillis() - bindStartTime;
                log.info("  ✓ Successfully bound as {} in {}ms", principal, bindDuration);
                return LdapAuthResult.success(domain, null);
            } catch (AuthenticationException e) {
                long bindDuration = System.currentTimeMillis() - bindStartTime;
                lastError = handleAuthenticationException(e, username, principal, domain);
                log.warn("  ✗ [{}/{}] Authentication failed for {} in {}ms: {}", 
                        attemptNumber, principals.size(), principal, bindDuration, lastError.getErrorMessage());
            } catch (CommunicationException e) {
                long bindDuration = System.currentTimeMillis() - bindStartTime;
                lastError = handleCommunicationException(e, username, domain);
                log.error("  ✗ [{}/{}] Communication error for {} in {}ms: {}", 
                        attemptNumber, principals.size(), principal, bindDuration, e.getMessage());
                log.error("  ⚠ Stopping further bind attempts due to communication error");
                break;
            } catch (Exception e) {
                long bindDuration = System.currentTimeMillis() - bindStartTime;
                lastError = handleGeneralException(e, username, principal, domain);
                log.debug("  ✗ [{}/{}] Bind failed for {} in {}ms: {} - {}", 
                        attemptNumber, principals.size(), principal, bindDuration, 
                        e.getClass().getSimpleName(), e.getMessage());
            }
        }
        
        log.warn("All {} bind attempts failed", principals.size());
        return lastError != null ? lastError : LdapAuthResult.failure("Bind failed for domain: " + domain);
    }

    /**
     * 搜索用户信息
     */
    private LdapAuthResult searchUser(String username, String domain, String dnsName,
                                       String baseDn, String serverAddr, LdapContextSource contextSource) {
        long searchStartTime = System.currentTimeMillis();
        try {
            LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
            String filter = buildUserSearchFilter(username, domain, dnsName);
            
            log.info("Starting LDAP user search:");
            log.info("  Search Base: {}", baseDn);
            log.info("  Search Filter: {}", filter);
            log.info("  Search Scope: SUBTREE");

            List<LdapUserInfo> results = ldapTemplate.search(
                    query()
                        .base("")  
                        .searchScope(SearchScope.SUBTREE)
                        .filter(filter),
                    userAttributesMapper(serverAddr)
            );

            long searchDuration = System.currentTimeMillis() - searchStartTime;
            log.info("Search completed in {}ms, found {} result(s)", searchDuration, results.size());

            if (results.isEmpty()) {
                log.warn("✗ User {} not found in domain {} after {}ms", username, domain, searchDuration);
                log.debug("Search filter used: {}", filter);
                return LdapAuthResult.failure("User not found in domain: " + domain);
            }

            LdapUserInfo userInfo = results.get(0);
            log.info("✓ User found successfully:");
            log.info("  Display Name: {}", userInfo.getDisplayName());
            log.info("  Email: {}", userInfo.getEmail());
            log.info("  UPN: {}", userInfo.getUserPrincipalName());
            log.info("  SAM Account: {}", userInfo.getSAMAccountName());
            log.info("  Department: {}", userInfo.getDepartment());
            log.info("  Company: {}", userInfo.getCompany());
            log.debug("  DN: {}", userInfo.getDn());
            log.debug("  Member Of Groups: {}", userInfo.getMemberOf() != null ? userInfo.getMemberOf().size() : 0);
            
            return LdapAuthResult.success(domain, userInfo);

        } catch (Exception e) {
            long searchDuration = System.currentTimeMillis() - searchStartTime;
            log.error("✗ Exception during LDAP search in domain {} after {}ms", domain, searchDuration);
            log.error("Exception type: {}", e.getClass().getName());
            log.error("Exception message: {}", e.getMessage());
            log.debug("Stack trace:", e);
            return LdapAuthResult.failure("Search failed: " + e.getMessage());
        }
    }

    /**
     * 构建登录主体列表
     */
    private List<String> buildPrincipals(String username, String domain, String dnsName) {
        return List.of(
                username + "@" + domain,
                username + "@" + dnsName,
                domain + "\\" + username
        );
    }

    /**
     * 构建用户搜索过滤器
     */
    private String buildUserSearchFilter(String username, String domain, String dnsName) {
        String safeSam = escapeForLdap(username);
        String safeUpnDomain = escapeForLdap(username + "@" + domain);
        String safeUpnDns = escapeForLdap(username + "@" + dnsName);
        return String.format(USER_SEARCH_TEMPLATE, safeSam, safeUpnDomain, safeUpnDns);
    }

    /**
     * 从BaseDn生成DNS名称
     */
    private String makeDnsFromBaseDn(String baseDn) {
        String[] segments = baseDn.split(",");
        List<String> parts = new ArrayList<>();
        for (String seg : segments) {
            seg = seg.trim();
            if (seg.toLowerCase().startsWith("dc=")) {
                parts.add(seg.substring(3));
            }
        }
        return String.join(".", parts);
    }

    /**
     * 构建LDAP上下文源
     */
    private LdapContextSource buildContextSource(LdapConfigurationProperties.LdapDomainConfig config) {
        log.debug("Building LDAP context source...");
        LdapContextSource contextSource = new LdapContextSource();
        
        String protocol = config.isUseSsl() ? LdapConstants.Protocol.LDAPS : LdapConstants.Protocol.LDAP;
        String url = protocol + config.getLdapServer().trim() + ":" + config.getLdapPort();
        
        log.debug("  LDAP URL: {}", url);
        log.debug("  Base DN: {}", config.getBaseDn().trim());
        
        contextSource.setUrl(url);
        contextSource.setBase(config.getBaseDn().trim());
        
        Hashtable<String, Object> baseEnvironment = new Hashtable<>();
        baseEnvironment.put("com.sun.jndi.ldap.connect.timeout", String.valueOf(config.getConnectTimeout()));
        baseEnvironment.put("com.sun.jndi.ldap.read.timeout", String.valueOf(config.getReadTimeout()));
        baseEnvironment.put("java.naming.referral", "follow");
        
        log.debug("  Environment properties: connect.timeout={}, read.timeout={}, referral=follow", 
                config.getConnectTimeout(), config.getReadTimeout());
        
        if (config.isUseSsl() && config.isTrustAllCertificates()) {
            log.warn("SSL certificate validation is DISABLED for LDAP connection");
            try {
                TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
                };
                
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new SecureRandom());
                
                baseEnvironment.put("java.naming.ldap.factory.socket", CustomSSLSocketFactory.class.getName());
                CustomSSLSocketFactory.setSslContext(sslContext);
            } catch (Exception e) {
                log.error("Failed to configure SSL trust manager: {}", e.getMessage(), e);
            }
        }
        
        contextSource.setBaseEnvironmentProperties(baseEnvironment);
        
        return contextSource;
    }

    /**
     * LDAP特殊字符转义
     */
    private String escapeForLdap(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                    .replace("*", "\\*")
                    .replace("(", "\\(")
                    .replace(")", "\\)");
    }

    /**
     * LDAP属性映射器
     */
    private AttributesMapper<LdapUserInfo> userAttributesMapper(String serverAddr) {
        return attrs -> {
            String dn = getAttributeValue(attrs, DISTINGUISHED_NAME);
            String cn = getAttributeValue(attrs, CN);
            String displayName = getAttributeValue(attrs, DISPLAY_NAME);
            String mail = getAttributeValue(attrs, MAIL);
            String userPrincipalName = getAttributeValue(attrs, USER_PRINCIPAL_NAME);
            String sAMAccountName = getAttributeValue(attrs, SAM_ACCOUNT_NAME);
            String department = getAttributeValue(attrs, DEPARTMENT);
            String company = getAttributeValue(attrs, COMPANY);
            List<String> memberOf = extractMemberOf(attrs);
            String path = dn != null ? String.format("LDAP://%s/%s", serverAddr, dn) : null;

            return LdapUserInfo.builder()
                    .path(path)
                    .cn(cn)
                    .displayName(displayName)
                    .email(mail)
                    .userPrincipalName(userPrincipalName)
                    .sAMAccountName(sAMAccountName)
                    .memberOf(memberOf)
                    .dn(dn)
                    .department(department)
                    .company(company)
                    .build();
        };
    }

    /**
     * 提取memberOf属性
     */
    private List<String> extractMemberOf(Attributes attrs) {
        List<String> memberOf = new ArrayList<>();
        try {
            if (attrs.get(MEMBER_OF) != null) {
                for (int i = 0; i < attrs.get(MEMBER_OF).size(); i++) {
                    Object value = attrs.get(MEMBER_OF).get(i);
                    if (value != null) {
                        memberOf.add(value.toString());
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract memberOf attribute: {}", e.getMessage());
        }
        return memberOf;
    }

    /**
     * 获取LDAP属性值
     */
    private String getAttributeValue(Attributes attrs, String name) {
        try {
            return attrs.get(name) != null ? attrs.get(name).get().toString() : null;
        } catch (Exception e) {
            log.debug("Failed to get attribute {}: {}", name, e.getMessage());
            return null;
        }
    }

    /**
     * 处理认证异常
     */
    private LdapAuthResult handleAuthenticationException(AuthenticationException e, 
                                                          String username, String principal, String domain) {
        String errorMsg = e.getMessage();
        Throwable rootCause = e.getRootCause();
        
        if (rootCause instanceof javax.naming.AuthenticationException) {
            String causeMsg = rootCause.getMessage();
            
            if (causeMsg != null) {
                if (causeMsg.contains("775") || causeMsg.contains("account is locked") 
                        || causeMsg.contains("locked out")) {
                    log.warn("Account locked for user {} in domain {}", username, domain);
                    return LdapAuthResult.failure("账户已被锁定，请联系管理员");
                }
                
                if (causeMsg.contains("532") || causeMsg.contains("password has expired")) {
                    log.warn("Password expired for user {} in domain {}", username, domain);
                    return LdapAuthResult.failure("密码已过期，请重置密码");
                }
                
                if (causeMsg.contains("52e") || causeMsg.contains("invalid credentials")) {
                    log.warn("Invalid credentials for user {} in domain {}", username, domain);
                    return LdapAuthResult.failure("用户名或密码错误");
                }
                
                if (causeMsg.contains("525") || causeMsg.contains("user not found")) {
                    log.warn("User {} not found in domain {}", username, domain);
                    return LdapAuthResult.failure("用户不存在");
                }
                
                if (causeMsg.contains("533") || causeMsg.contains("account disabled")) {
                    log.warn("Account disabled for user {} in domain {}", username, domain);
                    return LdapAuthResult.failure("账户已被禁用");
                }
            }
        }
        
        log.warn("Authentication failed for user {} with principal {}: {}", username, principal, errorMsg);
        return LdapAuthResult.failure("认证失败: " + (errorMsg != null ? errorMsg : "未知错误"));
    }

    /**
     * 处理通信异常
     */
    private LdapAuthResult handleCommunicationException(CommunicationException e, 
                                                         String username, String domain) {
        log.error("LDAP server communication error for user {} in domain {}: {}", 
                username, domain, e.getMessage());
        return LdapAuthResult.failure("无法连接到LDAP服务器，请稍后重试");
    }

    /**
     * 处理一般异常
     */
    private LdapAuthResult handleGeneralException(Exception e, String username, 
                                                   String principal, String domain) {
        String errorMsg = e.getMessage();
        
        if (e instanceof NamingException) {
            log.warn("LDAP naming exception for user {} with principal {}: {}", 
                    username, principal, errorMsg);
            return LdapAuthResult.failure("LDAP查询错误: " + errorMsg);
        }
        
        if (e instanceof AuthenticationNotSupportedException) {
            log.error("Authentication method not supported for user {} in domain {}", username, domain);
            return LdapAuthResult.failure("不支持的认证方式");
        }
        
        log.debug("Bind attempt failed for principal {}: {}", principal, errorMsg);
        return LdapAuthResult.failure("绑定失败: " + errorMsg);
    }
    
    public static class CustomSSLSocketFactory extends SSLSocketFactory {
        private static SSLContext sslContext;
        
        public static void setSslContext(SSLContext context) {
            sslContext = context;
        }
        
        private SSLSocketFactory getDelegate() {
            if (sslContext == null) {
                try {
                    sslContext = SSLContext.getDefault();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to get default SSL context", e);
                }
            }
            return sslContext.getSocketFactory();
        }
        
        @Override
        public String[] getDefaultCipherSuites() {
            return getDelegate().getDefaultCipherSuites();
        }
        
        @Override
        public String[] getSupportedCipherSuites() {
            return getDelegate().getSupportedCipherSuites();
        }
        
        @Override
        public java.net.Socket createSocket(java.net.Socket socket, String host, int port, boolean autoClose)
                throws java.io.IOException {
            return getDelegate().createSocket(socket, host, port, autoClose);
        }
        
        @Override
        public java.net.Socket createSocket(String host, int port) throws java.io.IOException {
            return getDelegate().createSocket(host, port);
        }
        
        @Override
        public java.net.Socket createSocket(String host, int port, java.net.InetAddress localHost, int localPort)
                throws java.io.IOException {
            return getDelegate().createSocket(host, port, localHost, localPort);
        }
        
        @Override
        public java.net.Socket createSocket(java.net.InetAddress host, int port) throws java.io.IOException {
            return getDelegate().createSocket(host, port);
        }
        
        @Override
        public java.net.Socket createSocket(java.net.InetAddress address, int port,
                java.net.InetAddress localAddress, int localPort) throws java.io.IOException {
            return getDelegate().createSocket(address, port, localAddress, localPort);
        }
    }
}
