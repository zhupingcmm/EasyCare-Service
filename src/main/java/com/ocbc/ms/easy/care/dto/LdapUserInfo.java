package com.ocbc.ms.easy.care.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LdapUserInfo {
    
    @JsonProperty("lanId")
    private String lanId;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("department")
    private String department;
    
    @JsonProperty("jobTitle")
    private String jobTitle;
    
    @JsonProperty("manager")
    private LdapUserInfo manager;
    
    @JsonProperty("managerLanId")
    private String managerLanId;
    
    @JsonProperty("workPhone")
    private String workPhone;
    
    @JsonProperty("officeLocation")
    private String officeLocation;
    
    @JsonProperty("photoURL")
    private String photoURL;
    
    @JsonProperty("cellPhone")
    private String cellPhone;
    
    @JsonProperty("userPrincipalName")
    private String userPrincipalName;
    
    @JsonProperty("exist")
    private boolean exist;
}
