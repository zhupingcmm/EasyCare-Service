package com.ocbc.ms.easy.care.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "user.role")
public class UserRoleConfigurationProperties {

    private String hrDepartment = "CHN E2P Human Resources";
}
