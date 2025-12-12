package com.ocbc.ms.easy.care.mapper;

import com.ocbc.ms.easy.care.constants.CommonConstants;
import com.ocbc.ms.easy.care.dto.LdapUserInfo;
import com.ocbc.ms.easy.care.service.LdapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.core.AttributesMapper;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.List;

@Slf4j
public class LdapUserInfoAttributesMapper implements AttributesMapper<LdapUserInfo> {

    private final boolean showManager;
    private final LdapService ldapService;
    
    public LdapUserInfoAttributesMapper() {
        this.showManager = false;
        this.ldapService = null;
    }
    
    public LdapUserInfoAttributesMapper(boolean showManager, LdapService ldapService) {
        this.showManager = showManager;
        this.ldapService = ldapService;
    }

    @Override
    public LdapUserInfo mapFromAttributes(Attributes attrs) throws NamingException {
        LdapUserInfo userInfo = LdapUserInfo.builder().build();

        userInfo.setLanId(getAttribute(attrs, CommonConstants.SAM_ACCOUNT_NAME));
        userInfo.setEmail(getAttribute(attrs, CommonConstants.MAIL));
        userInfo.setDepartment(getAttribute(attrs, CommonConstants.DEPARTMENT));
        userInfo.setOfficeLocation(getAttribute(attrs, "officeLocation"));
        userInfo.setUserPrincipalName(getAttribute(attrs, CommonConstants.USER_PRINCIPAL_NAME));
        userInfo.setWorkPhone(getAttribute(attrs, "telephoneNumber"));
        userInfo.setPhotoURL("");
        userInfo.setName(getAttribute(attrs, CommonConstants.DISPLAY_NAME));
        userInfo.setCellPhone(getAttribute(attrs, "mobile"));
        userInfo.setJobTitle(getAttribute(attrs, "title"));

        if (showManager && attrs.get("manager") != null) {
            String managerDn = (String) attrs.get("manager").get();
            userInfo.setManagerLanId(getIdFromDN(managerDn));

            // 递归获取manager信息需要通过LdapService
            if (ldapService != null) {
                try {
                    List<LdapUserInfo> managers = ldapService.getUserInfo(
                        getIdFromDN(managerDn), 
                        new LdapUserInfoAttributesMapper(false, ldapService)
                    );
                    if (!managers.isEmpty()) {
                        userInfo.setManager(managers.getFirst());
                    }
                } catch (Exception e) {
                    log.warn("Failed to get manager info for: {}", getIdFromDN(managerDn));
                }
            }
        }

        return userInfo;
    }

    private String getAttribute(Attributes attrs, String key) {
        try {
            Attribute attribute = attrs.get(key);
            return attribute != null ? (String) attribute.get() : "";
        } catch (Exception e) {
            log.debug("Failed to get attribute: {}", key);
            return "";
        }
    }

    private String getIdFromDN(String dn) {
        if (dn == null || dn.isEmpty()) {
            return "";
        }
        String[] parts = dn.split(",");
        for (String part : parts) {
            if (part.trim().startsWith("CN=")) {
                return part.trim().substring(3);
            }
        }
        return "";
    }
}
