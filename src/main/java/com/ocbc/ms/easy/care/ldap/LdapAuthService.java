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
import java.util.ArrayList;
import java.util.List;

import static com.ocbc.ms.easy.care.ldap.LdapConstants.Attributes.*;
import static com.ocbc.ms.easy.care.ldap.LdapConstants.Filter.USER_SEARCH_TEMPLATE;
import static com.ocbc.ms.easy.care.ldap.LdapConstants.Protocol.LDAP;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Slf4j
@Service
@RequiredArgsConstructor
public class LdapAuthService {

    private final LdapConfigurationProperties ldapProps;

    public LdapAuthResult authenticate(String username, String password) {
        LdapAuthResult validationResult = validateCredentials(username, password);
        if (validationResult != null) {
            return validationResult;
        }

        for (LdapConfigurationProperties.LdapDomainConfig domainConfig : ldapProps.getLdap()) {
            LdapAuthResult result = authenticateWithDomain(username, password, domainConfig);
            if (result.isSuccess()) {
                return result;
            }
        }

        log.warn("Failed to authenticate user {} with all configured domains", username);
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
        String domain = domainConfig.getDomain().trim();
        String serverAddr = domainConfig.getLdapServer().trim();
        int port = domainConfig.getLdapPort();
        String baseDn = domainConfig.getBaseDn().trim();
        String dnsName = makeDnsFromBaseDn(baseDn);

        log.info("Attempting LDAP authentication for user {} with domain {}", username, domain);

        try {
            LdapContextSource contextSource = buildContextSource(serverAddr, port, baseDn);
            LdapAuthResult bindResult = attemptBind(username, password, domain, dnsName, contextSource);

            if (!bindResult.isSuccess()) {
                return bindResult;
            }

            return searchUser(username, domain, dnsName, baseDn, serverAddr, contextSource);
        } catch (Exception e) {
            log.error("Unexpected error during LDAP authentication for user {} in domain {}: {}", 
                    username, domain, e.getMessage(), e);
            return LdapAuthResult.failure("Authentication error: " + e.getMessage());
        }
    }

    /**
     * 尝试绑定LDAP
     */
    private LdapAuthResult attemptBind(String username, String password, String domain,
                                       String dnsName, LdapContextSource contextSource) {
        List<String> principals = buildPrincipals(username, domain, dnsName);
        LdapAuthResult lastError = null;

        for (String principal : principals) {
            try {
                contextSource.setUserDn(principal);
                contextSource.setPassword(password);
                contextSource.afterPropertiesSet();
                contextSource.getContext(principal, password);
                log.info("Successfully bound as {}", principal);
                return LdapAuthResult.success(domain, null);
            } catch (AuthenticationException e) {
                lastError = handleAuthenticationException(e, username, principal, domain);
                log.warn("Authentication failed for principal {}: {}", principal, lastError.getErrorMessage());
            } catch (CommunicationException e) {
                lastError = handleCommunicationException(e, username, domain);
                log.error("Communication error for principal {}: {}", principal, e.getMessage());
            } catch (Exception e) {
                lastError = handleGeneralException(e, username, principal, domain);
                log.debug("Bind failed for principal {}: {}", principal, e.getMessage());
            }
        }
        
        return lastError != null ? lastError : LdapAuthResult.failure("Bind failed for domain: " + domain);
    }

    /**
     * 搜索用户信息
     */
    private LdapAuthResult searchUser(String username, String domain, String dnsName,
                                       String baseDn, String serverAddr, LdapContextSource contextSource) {
        try {
            LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
            String filter = buildUserSearchFilter(username, domain, dnsName);

            List<LdapUserInfo> results = ldapTemplate.search(
                    query().base(baseDn).filter(filter),
                    userAttributesMapper(serverAddr)
            );

            if (results.isEmpty()) {
                log.warn("User {} not found in domain {}", username, domain);
                return LdapAuthResult.failure("User not found in domain: " + domain);
            }

            LdapUserInfo userInfo = results.get(0);
            log.info("LDAP authentication successful for user {} in domain {}", username, domain);
            return LdapAuthResult.success(domain, userInfo);

        } catch (Exception e) {
            log.error("Exception during LDAP search in domain {}: {}", domain, e.getMessage(), e);
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
    private LdapContextSource buildContextSource(String serverAddr, int port, String baseDn) {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(LDAP + serverAddr + ":" + port);
        contextSource.setBase(baseDn);
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
}
