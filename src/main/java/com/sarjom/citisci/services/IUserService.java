package com.sarjom.citisci.services;

import com.sarjom.citisci.bos.ProjectBO;
import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.dtos.CreateUserRequestDTO;
import com.sarjom.citisci.dtos.CreateUserResponseDTO;
import com.sarjom.citisci.dtos.InviteUserRequestDTO;
import com.sarjom.citisci.dtos.InviteUserResponseDTO;

public interface IUserService {
    CreateUserResponseDTO createUser(CreateUserRequestDTO createUserRequestDTO) throws Exception;

    InviteUserResponseDTO inviteUser(InviteUserRequestDTO inviteUserRequestDTO, UserBO userBO) throws Exception;

    void processInviteUsers(InviteUserRequestDTO inviteUserRequestDTO, UserBO userBO, ProjectBO projectBO) throws Exception;
}
