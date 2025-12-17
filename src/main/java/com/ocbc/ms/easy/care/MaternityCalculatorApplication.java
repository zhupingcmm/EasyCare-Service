package com.ocbc.ms.easy.care;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.ldap.LdapRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

/**
 * 产假和生育津贴计算系统主启动类
 */
@SpringBootApplication(exclude = {LdapRepositoriesAutoConfiguration.class})
@EnableScheduling
public class MaternityCalculatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaternityCalculatorApplication.class, args);
    }

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(new JdbcTemplate(dataSource));
    }
}