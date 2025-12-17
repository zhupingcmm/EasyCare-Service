package com.ocbc.ms.easy.care.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class LdapConfig {

    private final LdapConfigurationProperties ldapProps;

    @Bean(name = "ldapAuthProvider")
    public ActiveDirectoryLdapAuthenticationProvider adAuthProvider() {
        String domain = ldapProps.getDomain();
        String url = ldapProps.getUrl();

        log.info("Initializing ActiveDirectoryLdapAuthenticationProvider with domain: {} and url: {}", domain, url);
        
        ActiveDirectoryLdapAuthenticationProvider adProvider = new ActiveDirectoryLdapAuthenticationProvider(domain, url);
        adProvider.setConvertSubErrorCodesToExceptions(true);
        adProvider.setUseAuthenticationRequestCredentials(true);
        
        return adProvider;
    }

    @Bean(name = "ldapTemplate")
    public LdapTemplate initLdapTemplate() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapProps.getUrl());
        contextSource.setBase(ldapProps.getSearchBase());
        
        contextSource.setCacheEnvironmentProperties(false);
        contextSource.setPooled(true);
        contextSource.setAnonymousReadOnly(false);
        
        if (ldapProps.getUsername() != null && !ldapProps.getUsername().isBlank() 
            && ldapProps.getPassword() != null && !ldapProps.getPassword().isBlank()) {
            log.info("Configuring LDAP with authentication for user: {}", ldapProps.getUsername());
            contextSource.setAuthenticationSource(createAuthenticationSource());
        } else {
            log.info("Configuring LDAP with anonymous access");
        }
        
        try {
            contextSource.afterPropertiesSet();
        } catch (Exception e) {
            log.error("Failed to initialize LDAP context source", e);
            throw new RuntimeException("LDAP configuration error", e);
        }
        
        LdapTemplate template = new LdapTemplate(contextSource);
        template.setIgnorePartialResultException(true);
        template.setDefaultTimeLimit(5000);
        template.setDefaultCountLimit(100);
        
        return template;
    }
    
    private AuthenticationSource createAuthenticationSource() {
        return new AuthenticationSource() {
            @Override
            public String getPrincipal() {
                StringBuilder dn = new StringBuilder("CN=").append(ldapProps.getUsername());
                if (ldapProps.getOrgUnit() != null && !ldapProps.getOrgUnit().isBlank()) {
                    dn.append(",OU=").append(ldapProps.getOrgUnit());
                }
                dn.append(",").append(ldapProps.getSearchBase());
                return dn.toString();
            }

            @Override
            public String getCredentials() {
                return ldapProps.getPassword();
            }
        };
    }

}
