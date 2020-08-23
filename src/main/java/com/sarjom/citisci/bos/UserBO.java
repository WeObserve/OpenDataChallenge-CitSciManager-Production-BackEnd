package com.sarjom.citisci.bos;

import com.sarjom.citisci.enums.Role;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class UserBO {
    String id;
    String name;
    String email;
    String password;
    String plainTextPassword;
    Role role;
    List<OrganisationBO> organisations;
}
