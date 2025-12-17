package com.ocbc.ms.easy.care.mapper;

import com.ocbc.ms.easy.care.constants.CommonConstants;
import com.ocbc.ms.easy.care.dto.AdGroupResp;
import com.ocbc.ms.easy.care.util.LdapUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.core.AttributesMapper;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.ldap.Rdn;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class AdGroupAttributesMapper implements AttributesMapper<AdGroupResp> {

    @Override
    public AdGroupResp mapFromAttributes(Attributes attributes) throws NamingException {
        List<String> adGroupList = new ArrayList<>();
        AdGroupResp adGroupResp = new AdGroupResp();
        
        try {
            if (attributes.get(CommonConstants.MEMBEROF) != null) {
                NamingEnumeration<?> memberOfEnum = attributes.get(CommonConstants.MEMBEROF).getAll();
                
                while (memberOfEnum.hasMore()) {
                    try {
                        String adGroupDn = (String) memberOfEnum.next();
                        String cn = extractCnFromDn(adGroupDn);
                        
                        if (cn != null && !cn.isEmpty()) {
                            adGroupList.add(cn);
                            log.debug("Extracted AD group CN: {}", cn);
                        }
                    } catch (Exception e) {
                        log.warn("Failed to process AD group entry", e);
                    }
                }
            } else {
                log.debug("No memberOf attribute found");
            }
        } catch (Exception e) {
            log.error("Error processing AD group attributes", e);
        }
        
        String adGroupStr = adGroupList.stream()
                .sorted()
                .distinct()
                .collect(Collectors.joining("<br/>"));
        
        adGroupResp.setAdGroups(adGroupStr);
        log.debug("Mapped {} AD groups", adGroupList.size());
        
        return adGroupResp;
    }
    
    private String extractCnFromDn(String dn) {
        if (dn == null || dn.isEmpty()) {
            return null;
        }
        
        try {
            String ldapName = LdapUtils.newLdapName(dn);
            List<Rdn> rdnList = LdapUtils.getRdns(ldapName);
            
            for (Rdn rdn : rdnList) {
                if (CommonConstants.CN.equals(rdn.getType())) {
                    return String.valueOf(rdn.getValue());
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract CN from DN: {}", dn, e);
        }
        
        return null;
    }
}
