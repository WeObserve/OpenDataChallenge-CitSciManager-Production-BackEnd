package com.sarjom.citisci.bos;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserBO {
    String id;
    String name;
    String email;
    String password;
    String plainTextPassword;
    String orgName;
    String orgAffiliation;
}
