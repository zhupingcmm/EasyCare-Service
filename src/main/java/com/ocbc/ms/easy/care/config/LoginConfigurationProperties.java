package com.ocbc.ms.easy.care.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "login")
public class LoginConfigurationProperties {

    /**
     * LDAP认证开关
     */
    private Ldap ldap = new Ldap();

    /**
     * Mock登录配置
     */
    private Mock mock = new Mock();

    @Data
    public static class Ldap {
        /**
         * 是否启用LDAP认证
         */
        private boolean enabled = false;
    }

    @Data
    public static class Mock {
        /**
         * 是否允许Mock登录（跳过RSA解密）
         */
        private boolean enabled = false;
    }
}
