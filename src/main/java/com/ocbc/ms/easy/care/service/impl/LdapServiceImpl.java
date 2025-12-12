package com.ocbc.ms.easy.care.service.impl;

import com.ocbc.ms.easy.care.constants.CommonConstants;
import com.ocbc.ms.easy.care.dto.AdGroupResp;
import com.ocbc.ms.easy.care.dto.LdapUserInfo;
import com.ocbc.ms.easy.care.mapper.AdGroupAttributesMapper;
import com.ocbc.ms.easy.care.service.LdapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Slf4j
@Service
@RequiredArgsConstructor
public class LdapServiceImpl implements LdapService {

    private final LdapTemplate ldapTemplate;
    private final ActiveDirectoryLdapAuthenticationProvider ldapAuthProvider;

    @Override
    public AdGroupResp searchAdGroup(String lanId) {
        try {
            if (ldapTemplate == null) {
                log.warn("LdapTemplate is not configured, skipping AD group search for lanId: {}", lanId);
                return new AdGroupResp();
            }

            LdapQuery queryResult = query()
                    .where(CommonConstants.SAM_ACCOUNT_NAME)
                    .is(lanId);
            
            List<AdGroupResp> results = ldapTemplate.search(queryResult, new AdGroupAttributesMapper());
            
            if (results.isEmpty()) {
                log.info("LdapServiceImpl Exception: User not found");
                throw new RuntimeException("Can find this user");
            }
            
            AdGroupResp resp = results.getFirst();
            resp.setLanId(lanId);
            
            return resp;
        } catch (Exception e) {
            log.info("LdapServiceImpl Exception={}", e.getMessage());
            log.info("Enterprise:chatbot-fulfillment-hr: fail to searchAdGroup");
            throw new RuntimeException("Can find this user", e);
        }
    }

    @Override
    public List<LdapUserInfo> getUserInfo(String lanId, AttributesMapper<LdapUserInfo> attributesMapper) {
        try {
            if (ldapTemplate == null) {
                log.warn("LdapTemplate is not configured, skipping user info search for lanId: {}", lanId);
                return Collections.emptyList();
            }
            
            LdapQuery queryResult = query()
                    .where(CommonConstants.SAM_ACCOUNT_NAME)
                    .is(lanId);
            
            List<LdapUserInfo> results = ldapTemplate.search(queryResult, attributesMapper);
            
            if (results.isEmpty()) {
                log.info("User not found for lanId: {}", lanId);
                return Collections.emptyList();
            }
            
            return results;
        } catch (Exception e) {
            log.error("Failed to get user info for lanId: {}, error: {}", lanId, e.getMessage(), e);
            throw new RuntimeException("Failed to get user info", e);
        }
    }
    
    @Override
    public Boolean validateUserAndPassword(String lanId, String password) {
        try {
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(lanId, password);
            
            if (!authentication.isAuthenticated()) {
                authentication = (UsernamePasswordAuthenticationToken) ldapAuthProvider.authenticate(authentication);
            }
            
            LdapUserDetails userDetails = (LdapUserDetails) authentication.getPrincipal();
            return userDetails != null && StringUtils.isNotBlank(userDetails.getUsername());
        } catch (AuthenticationException e) {
            log.error("LDAP authentication failed for user: {}", lanId, e);
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during LDAP authentication for user: {}", lanId, e);
            return false;
        }
    }
}
