package com.hr.maternity.ldap;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LdapAuthResult {
    private boolean success;
    private String domain;
    private LdapUserInfo userInfo;
    private String errorMessage;
    
    public static LdapAuthResult failure(String errorMessage) {
        return LdapAuthResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
    
    public static LdapAuthResult success(String domain, LdapUserInfo userInfo) {
        return LdapAuthResult.builder()
                .success(true)
                .domain(domain)
                .userInfo(userInfo)
                .build();
    }
}
