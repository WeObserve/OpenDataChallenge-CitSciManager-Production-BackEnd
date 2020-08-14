package com.sarjom.citisci.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LoginRequestDTO {
    public String email;
    public String jwtEncryptedPassword;
}
