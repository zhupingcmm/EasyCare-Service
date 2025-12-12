package com.ocbc.ms.easy.care.service;

import com.ocbc.ms.easy.care.dto.AdGroupResp;
import com.ocbc.ms.easy.care.dto.LdapUserInfo;
import org.springframework.ldap.core.AttributesMapper;

import java.util.List;

public interface LdapService {
    Boolean validateUserAndPassword(String lanId, String password);
    AdGroupResp searchAdGroup(String lanId);
    List<LdapUserInfo> getUserInfo(String lanId, AttributesMapper<LdapUserInfo> attributesMapper);
}
