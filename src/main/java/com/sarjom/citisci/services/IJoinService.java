package com.sarjom.citisci.services;

import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.dtos.CreateJoinRequestDTO;
import com.sarjom.citisci.dtos.CreateJoinResponseDTO;

public interface IJoinService {
    CreateJoinResponseDTO createJoin(CreateJoinRequestDTO createJoinRequestDTO, UserBO userBO) throws Exception;
}
