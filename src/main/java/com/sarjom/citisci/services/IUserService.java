package com.sarjom.citisci.services;

import com.sarjom.citisci.dtos.CreateUserRequestDTO;
import com.sarjom.citisci.dtos.CreateUserResponseDTO;

public interface IUserService {
    CreateUserResponseDTO createUser(CreateUserRequestDTO createUserRequestDTO) throws Exception;
}
