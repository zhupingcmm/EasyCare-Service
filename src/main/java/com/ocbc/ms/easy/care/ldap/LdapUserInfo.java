package com.ocbc.ms.easy.care.ldap;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LdapUserInfo {
    private String path;
    private String cn;
    private String displayName;
    private String email;
    private String userPrincipalName;
    private String sAMAccountName;
    private List<String> memberOf;
    private String dn;
    private String department;
    private String company;
}
