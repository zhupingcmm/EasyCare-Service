package com.ocbc.ms.easy.care;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.ldap.LdapRepositoriesAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 产假和生育津贴计算系统主启动类
 */
@SpringBootApplication(exclude = {LdapRepositoriesAutoConfiguration.class})
@EnableScheduling
public class MaternityCalculatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaternityCalculatorApplication.class, args);
    }
}