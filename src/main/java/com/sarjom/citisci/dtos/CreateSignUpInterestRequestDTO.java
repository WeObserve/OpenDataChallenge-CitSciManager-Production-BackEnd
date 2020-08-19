package com.sarjom.citisci.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateSignUpInterestRequestDTO {
    public String description;
    public String emailList;
}
