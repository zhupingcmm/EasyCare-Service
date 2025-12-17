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
        if (StringUtils.isBlank(lanId)) {
            log.warn("searchAdGroup called with blank lanId");
            return new AdGroupResp();
        }
        
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
                log.warn("AD group not found for lanId: {}", lanId);
                throw new RuntimeException("User not found in AD: " + lanId);
            }
            
            AdGroupResp resp = results.getFirst();
            resp.setLanId(lanId);
            
            log.debug("Successfully retrieved AD groups for lanId: {}", lanId);
            return resp;
        } catch (Exception e) {
            log.error("Failed to search AD group for lanId: {}, error: {}", lanId, e.getMessage(), e);
            throw new RuntimeException("Failed to search AD group for user: " + lanId, e);
        }
    }

    @Override
    public List<LdapUserInfo> getUserInfo(String lanId, AttributesMapper<LdapUserInfo> attributesMapper) {
        if (StringUtils.isBlank(lanId)) {
            log.warn("getUserInfo called with blank lanId");
            return Collections.emptyList();
        }
        
        if (attributesMapper == null) {
            log.warn("getUserInfo called with null attributesMapper");
            return Collections.emptyList();
        }
        
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
                log.warn("User info not found for lanId: {}", lanId);
                return Collections.emptyList();
            }
            
            log.debug("Successfully retrieved user info for lanId: {}", lanId);
            return results;
        } catch (Exception e) {
            log.error("Failed to get user info for lanId: {}, error: {}", lanId, e.getMessage(), e);
            throw new RuntimeException("Failed to get user info for user: " + lanId, e);
        }
    }
    
    @Override
    public Boolean validateUserAndPassword(String lanId, String password) {
        if (StringUtils.isBlank(lanId) || StringUtils.isBlank(password)) {
            log.warn("validateUserAndPassword called with blank lanId or password");
            return false;
        }
        
        try {
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(lanId, password);
            
            if (!authentication.isAuthenticated()) {
                authentication = (UsernamePasswordAuthenticationToken) ldapAuthProvider.authenticate(authentication);
            }
            
            LdapUserDetails userDetails = (LdapUserDetails) authentication.getPrincipal();
            boolean isValid = userDetails != null && StringUtils.isNotBlank(userDetails.getUsername());
            
            if (isValid) {
                log.debug("LDAP authentication successful for user: {}", lanId);
            } else {
                log.warn("LDAP authentication failed: invalid user details for user: {}", lanId);
            }
            
            return isValid;
        } catch (AuthenticationException e) {
            log.warn("LDAP authentication failed for user: {}, reason: {}", lanId, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during LDAP authentication for user: {}", lanId, e);
            return false;
        }
    }
}
