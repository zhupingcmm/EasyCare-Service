package com.easy.care.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.ldap.LdapHealthContributorAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Slf4j
@Configuration
@AutoConfigureBefore(LdapHealthContributorAutoConfiguration.class)
public class LdapHealthConfig {

    @PostConstruct
    public void init() {
        log.info("LDAP Health Check configuration initialized");
    }

    @Configuration
    @ConditionalOnProperty(name = "login.ldap.enabled", havingValue = "false", matchIfMissing = true)
    public static class DisableLdapHealthCheck {
        
        @PostConstruct
        public void disableLdapHealth() {
            log.info("LDAP is disabled, skipping LDAP health check");
            System.setProperty("management.health.ldap.enabled", "false");
        }
    }

    @Configuration
    @ConditionalOnProperty(name = "login.ldap.enabled", havingValue = "true")
    public static class EnableLdapHealthCheck {
        
        @PostConstruct
        public void enableLdapHealth() {
            log.info("LDAP is enabled, LDAP health check is active");
        }
    }
}
