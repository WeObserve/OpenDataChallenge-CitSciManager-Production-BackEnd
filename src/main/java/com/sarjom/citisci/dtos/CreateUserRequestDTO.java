package com.sarjom.citisci.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateUserRequestDTO {
    public String email;
    public String name;
    public String orgName;
    public String orgAffiliation;
}