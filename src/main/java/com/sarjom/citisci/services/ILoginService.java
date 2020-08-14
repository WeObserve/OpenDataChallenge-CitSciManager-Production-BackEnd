package com.sarjom.citisci.services;

import com.sarjom.citisci.dtos.LoginRequestDTO;
import com.sarjom.citisci.dtos.LoginResponseDTO;

public interface ILoginService {
    LoginResponseDTO login(LoginRequestDTO loginRequestDTO) throws Exception;
}
