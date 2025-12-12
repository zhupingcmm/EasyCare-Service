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
        
        if (attributes.get(CommonConstants.MEMBEROF) != null) {
            NamingEnumeration<?> authorization = attributes.get(CommonConstants.MEMBEROF).getAll();
            while (authorization.hasMore()) {
                String adGroupDn = (String) authorization.next();
                String ldapName = LdapUtils.newLdapName(adGroupDn);
                List<Rdn> rdnList = LdapUtils.getRdns(ldapName);
                
                Map<String, String> rdnMap = rdnList.stream()
                        .collect(Collectors.groupingBy(
                                Rdn::getType,
                                Collectors.mapping(
                                        rdn -> String.valueOf(rdn.getValue()),
                                        Collectors.toList()
                                )
                        ))
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> String.join(", ", e.getValue())
                        ));
                
                String cn = rdnMap.get(CommonConstants.CN);
                if (cn != null) {
                    adGroupList.add(cn);
                }
            }
        }
        
        String adGroupStr = adGroupList.stream()
                .sorted()
                .collect(Collectors.joining("<br/>"));
        
        adGroupResp.setAdGroups(adGroupStr);
        
        return adGroupResp;
    }
}
