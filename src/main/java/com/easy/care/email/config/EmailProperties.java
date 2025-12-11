package com.easy.care.email.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 邮件配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.mail")
public class EmailProperties {

    /**
     * SMTP服务器地址
     */
    private String host;

    /**
     * SMTP服务器端口
     */
    private Integer port;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 发件人邮箱
     */
    private String from;

    /**
     * 是否启用SSL
     */
    private Boolean ssl = true;

    /**
     * 是否启用TLS
     */
    private Boolean tls = true;
}
