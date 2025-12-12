package com.ocbc.ms.easy.care.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Configuration
@ConfigurationProperties(prefix = "ad")
@Validated
public class LdapConfigurationProperties {

    @NotBlank(message = "AD URL cannot be blank")
    private String url;
    
    @NotBlank(message = "AD domain cannot be blank")
    private String domain;
    
    @NotBlank(message = "AD search base cannot be blank")
    private String searchBase;

    private String username;

    private String password;

    private String orgUnit;
}
