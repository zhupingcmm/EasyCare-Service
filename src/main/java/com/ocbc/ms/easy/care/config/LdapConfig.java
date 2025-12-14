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
        contextSource.setCacheEnvironmentProperties(false);
        contextSource.setUrl(ldapProps.getUrl());
        contextSource.setBase(ldapProps.getSearchBase());
        
        if (ldapProps.getUsername() != null && !ldapProps.getUsername().isBlank() 
            && ldapProps.getPassword() != null && !ldapProps.getPassword().isBlank()) {
            log.info("Configuring LDAP with authentication for user: {}", ldapProps.getUsername());
            contextSource.setAuthenticationSource(new AuthenticationSource() {
                @Override
                public String getPrincipal() {
                    if (ldapProps.getOrgUnit() != null && !ldapProps.getOrgUnit().isBlank()) {
                        return "CN=" + ldapProps.getUsername() + ",OU=" + ldapProps.getOrgUnit() + "," + ldapProps.getSearchBase();
                    } else {
                        return "CN=" + ldapProps.getUsername() + "," + ldapProps.getSearchBase();
                    }
                }

                @Override
                public String getCredentials() {
                    return ldapProps.getPassword();
                }
            });
        } else {
            log.info("Configuring LDAP with anonymous access");
        }
        
        LdapTemplate template = new LdapTemplate(contextSource);
        template.setIgnorePartialResultException(true);
        
        return template;
    }

}
