package com.sarjom.citisci.services.transactional;

import com.sarjom.citisci.bos.InviteUserRequestBO;
import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.dtos.CreateUserResponseDTO;
import com.sarjom.citisci.entities.User;

public interface IUserTransService {
    CreateUserResponseDTO createUserAndSendInvitationEmail(UserBO userBO, Boolean useTxn) throws Exception;

    void inviteUser(InviteUserRequestBO inviteUserRequestBO, Boolean useTxn) throws Exception;
}
