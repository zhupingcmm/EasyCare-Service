package com.ocbc.ms.easy.care.util;

import lombok.extern.slf4j.Slf4j;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class LdapUtils {
    
    private LdapUtils() {
    }
    
    public static String newLdapName(String adGroupDn) {
        try {
            LdapName ldapName = new LdapName(adGroupDn);
            return ldapName.toString();
        } catch (InvalidNameException e) {
            log.error("Invalid LDAP name: {}", adGroupDn, e);
            return adGroupDn;
        }
    }
    
    public static List<Rdn> getRdns(String ldapName) {
        try {
            LdapName name = new LdapName(ldapName);
            return name.getRdns();
        } catch (InvalidNameException e) {
            log.error("Failed to get RDNs from LDAP name: {}", ldapName, e);
            return new ArrayList<>();
        }
    }
}
