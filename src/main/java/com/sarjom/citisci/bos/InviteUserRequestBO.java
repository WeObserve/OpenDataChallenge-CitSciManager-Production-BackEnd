package com.sarjom.citisci.bos;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class InviteUserRequestBO {
    public String email;
    public String name;
    public String organisationId;
    public String projectId;
    public UserBO userBO;
    public String userId;
    public Boolean isUserCreationRequired;
    public Boolean isUserOrgMappingCreationRequired;
    public Boolean isUserProjectMappingCreationRequired;
}
