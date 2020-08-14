package com.sarjom.citisci.dtos;

import com.sarjom.citisci.bos.UserBO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateUserResponseDTO {
    public UserBO createdUser;
}
