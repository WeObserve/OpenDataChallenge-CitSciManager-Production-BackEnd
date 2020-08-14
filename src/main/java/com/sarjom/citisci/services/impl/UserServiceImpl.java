package com.sarjom.citisci.services.impl;

import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.db.mongo.daos.IUserDAO;
import com.sarjom.citisci.dtos.CreateUserRequestDTO;
import com.sarjom.citisci.dtos.CreateUserResponseDTO;
import com.sarjom.citisci.entities.User;
import com.sarjom.citisci.services.IUserService;
import com.sarjom.citisci.services.transactional.IUserTransService;
import com.sarjom.citisci.services.utilities.IHashService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements IUserService {
    private static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired IUserDAO userDAO;
    @Autowired IHashService hashService;
    @Autowired
    IUserTransService userTransService;

    @Override
    public CreateUserResponseDTO createUser(CreateUserRequestDTO createUserRequestDTO) throws Exception {
        logger.info("Inside createUser");

        validateCreateUserRequestDTO(createUserRequestDTO);

        checkThatUserWithThisEmailDoesntExist(createUserRequestDTO);

        UserBO userBO = createUserBO(createUserRequestDTO);

        CreateUserResponseDTO createUserResponseDTO = userTransService.createUserAndSendInvitationEmail(userBO, true);

        return createUserResponseDTO;
    }

    private void checkThatUserWithThisEmailDoesntExist(CreateUserRequestDTO createUserRequestDTO) throws Exception {
        logger.info("Inside checkThatUserWithThisEmailDoesntExist");

        List<User> usersWithGivenEmail = userDAO.getUsersByEmail(createUserRequestDTO.getEmail());

        if (!CollectionUtils.isEmpty(usersWithGivenEmail)) {
            throw new Exception("There is already a user with this email");
        }
    }

    private UserBO createUserBO(CreateUserRequestDTO createUserRequestDTO) throws Exception {
        logger.info("Inside createUserBO");

        UserBO userBO = new UserBO();

        BeanUtils.copyProperties(createUserRequestDTO, userBO);
        userBO.setId(new ObjectId().toHexString());

        populateEncodedPasswordInUserBO(userBO);

        return userBO;
    }

    private void populateEncodedPasswordInUserBO(UserBO userBO) throws Exception {
        logger.info("Inside populateEncodedPasswordInUserBO");

        String password = UUID.randomUUID().toString();
        String encodedPassword = hashService.getSha256HexString(password);

        userBO.setPassword(encodedPassword);
        userBO.setPlainTextPassword(password);
    }

    private void validateCreateUserRequestDTO(CreateUserRequestDTO createUserRequestDTO) throws Exception {
        logger.info("Inside validateCreateUserRequestDTO");

        if (createUserRequestDTO == null ||
                StringUtils.isEmpty(createUserRequestDTO.getEmail()) ||
                StringUtils.isEmpty(createUserRequestDTO.getName()) ||
                StringUtils.isEmpty(createUserRequestDTO.getOrgAffiliation()) ||
                StringUtils.isEmpty(createUserRequestDTO.getOrgName())) {
            throw new Exception("Invalid create user request");
        }
    }
}