package com.hr.maternity.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
@Validated
public class LdapConfigurationProperties {

    private List<LdapDomainConfig> ldap = new ArrayList<>();

    @Data
    @Validated
    public static class LdapDomainConfig {
        
        @NotBlank(message = "LDAP domain cannot be blank")
        private String domain;
        
        @NotBlank(message = "LDAP server address cannot be blank")
        private String ldapServer;
        
        @Min(value = 1, message = "LDAP port must be between 1 and 65535")
        @Max(value = 65535, message = "LDAP port must be between 1 and 65535")
        private int ldapPort = 389;
        
        @NotBlank(message = "LDAP base DN cannot be blank")
        private String baseDn;
        
        public boolean isValid() {
            return domain != null && !domain.isBlank()
                    && ldapServer != null && !ldapServer.isBlank()
                    && baseDn != null && !baseDn.isBlank();
        }
    }
}
