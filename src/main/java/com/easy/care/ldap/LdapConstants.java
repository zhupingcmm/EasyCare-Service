package com.easy.care.ldap;

public final class LdapConstants {
    
    private LdapConstants() {
    }
    
    public static final class Attributes {
        public static final String DISTINGUISHED_NAME = "distinguishedName";
        public static final String CN = "cn";
        public static final String DISPLAY_NAME = "displayName";
        public static final String MAIL = "mail";
        public static final String USER_PRINCIPAL_NAME = "userPrincipalName";
        public static final String SAM_ACCOUNT_NAME = "sAMAccountName";
        public static final String MEMBER_OF = "memberOf";
        public static final String DEPARTMENT = "department";
        public static final String COMPANY = "company";
        
        private Attributes() {
        }
    }
    
    public static final class Protocol {
        public static final String LDAP = "ldap://";
        public static final String LDAPS = "ldaps://";
        
        private Protocol() {
        }
    }
    
    public static final class Filter {
        public static final String USER_SEARCH_TEMPLATE = 
            "(|(sAMAccountName=%s)(userPrincipalName=%s)(userPrincipalName=%s))";
        
        private Filter() {
        }
    }
}
