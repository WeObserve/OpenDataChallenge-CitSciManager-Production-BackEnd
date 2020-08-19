package com.sarjom.citisci.dtos;

import com.sarjom.citisci.bos.SignUpInterestBO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateSignUpInterestResponseDTO {
    public SignUpInterestBO signUpInterest;
}
