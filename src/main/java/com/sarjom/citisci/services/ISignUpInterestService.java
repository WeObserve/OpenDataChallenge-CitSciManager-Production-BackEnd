package com.sarjom.citisci.services;

import com.sarjom.citisci.dtos.CreateSignUpInterestRequestDTO;
import com.sarjom.citisci.dtos.CreateSignUpInterestResponseDTO;

public interface ISignUpInterestService {
    CreateSignUpInterestResponseDTO createSignUpInterest(CreateSignUpInterestRequestDTO createSignUpInterestRequestDTO) throws Exception;
}
