package com.sarjom.citisci.services.transactional.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.mongodb.MongoClient;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.sarjom.citisci.bos.InviteUserRequestBO;
import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.db.mongo.daos.IUserDAO;
import com.sarjom.citisci.db.mongo.daos.IUserOrganisationMappingDAO;
import com.sarjom.citisci.db.mongo.daos.IUserProjectMappingDAO;
import com.sarjom.citisci.dtos.CreateUserResponseDTO;
import com.sarjom.citisci.entities.User;
import com.sarjom.citisci.entities.UserOrganisationMapping;
import com.sarjom.citisci.entities.UserProjectMapping;
import com.sarjom.citisci.enums.Role;
import com.sarjom.citisci.services.transactional.IUserTransService;
import com.sarjom.citisci.services.utilities.IAwsSesService;
import com.sarjom.citisci.services.utilities.IHashService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
public class UserTransServiceImpl implements IUserTransService {
    private static Logger logger = LoggerFactory.getLogger(UserTransServiceImpl.class);

    @Autowired
    @Qualifier("mongoCustom")
    MongoClient mongoClient;

    @Value("${mongo.database}")
    String databaseName;

    @Value("${invitation.email.link.endpoint}")
    String invitationEmailLinkEndpoint;

    @Value("${invitation.sender.email}")
    String invitationSenderEmail;

    @Value("${master.key}")
    String masterKey;

    @Autowired
    IUserDAO userDAO;

    @Autowired
    IAwsSesService awsSesService;

    @Autowired
    IHashService hashService;
    @Autowired
    IUserOrganisationMappingDAO userOrganisationMappingDAO;
    @Autowired
    IUserProjectMappingDAO userProjectMappingDAO;

    @Override
    public CreateUserResponseDTO createUserAndSendInvitationEmail(UserBO userBO, Boolean useTxn) throws Exception {
        logger.info("Inside createUserAndSendInvitationEmail");

        if (useTxn == null || !useTxn) {
            return createUserAndSendInvitationEmailWithoutTxn(userBO, null);
        }

        return createUserAndSendInvitationEmailWithTxn(userBO);
    }

    private CreateUserResponseDTO createUserAndSendInvitationEmailWithTxn(UserBO userBO) throws Exception {
        logger.info("Inside createUserAndSendInvitationEmailWithTxn");

        ClientSession clientSession = mongoClient.startSession();

        try {
            clientSession.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());

            CreateUserResponseDTO createUserResponseDTO = createUserAndSendInvitationEmailWithoutTxn(userBO, clientSession);

            clientSession.commitTransaction();
            clientSession.close();

            return createUserResponseDTO;
        } catch (Exception e) {
            clientSession.abortTransaction();
            clientSession.close();
            throw e;
        }
    }

    private CreateUserResponseDTO createUserAndSendInvitationEmailWithoutTxn(UserBO userBO, ClientSession clientSession) throws Exception {
        logger.info("Inside createUserAndSendInvitationEmailWithoutTxn");

        User user = new User();

        BeanUtils.copyProperties(userBO, user);

        user.setId(new ObjectId(userBO.getId()));

        userDAO.createUser(user, clientSession);

        //sendInvitationEmail(userBO.getPlainTextPassword(), userBO.getEmail());

        userBO.setPlainTextPassword(null);
        userBO.setPassword(null);

        CreateUserResponseDTO createUserResponseDTO = new CreateUserResponseDTO();
        createUserResponseDTO.setCreatedUser(userBO);

        return createUserResponseDTO;
    }

    private void sendInvitationEmail(String plainTextPassword, String recipientEmail, UserBO userBO) throws Exception {
        logger.info("Inside sendInvitationEmail");

        String message = "You have been invited to " + invitationEmailLinkEndpoint + " by " +
                userBO.getName() + ". Use " + plainTextPassword + " as your password to login";
        String subject = "Invitation from Greendubs";

        awsSesService.sendEmail(invitationSenderEmail, recipientEmail, subject, message);
    }

    @Override
    public void inviteUser(InviteUserRequestBO inviteUserRequestBO, Boolean useTxn) throws Exception {
        logger.info("Inside inviteUser");

        if (useTxn == null || !useTxn) {
            inviteUserWithoutTxn(inviteUserRequestBO, null);
            return;
        }

        inviteUserWithTxn(inviteUserRequestBO);
    }

    private void inviteUserWithTxn(InviteUserRequestBO inviteUserRequestBO) throws Exception {
        logger.info("Inside inviteUserWithTxn");

        ClientSession clientSession = mongoClient.startSession();

        try {
            clientSession.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());

            inviteUserWithoutTxn(inviteUserRequestBO, clientSession);

            clientSession.commitTransaction();
            clientSession.close();
        } catch (Exception e) {
            clientSession.abortTransaction();
            clientSession.close();
            throw e;
        }
    }

    private void inviteUserWithoutTxn(InviteUserRequestBO inviteUserRequestBO,  ClientSession clientSession) throws Exception {
        logger.info("Inside inviteUserWithoutTxn");

        Boolean isInviteUserRequestBOValid = validateInviteUserRequestBO(inviteUserRequestBO);

        if (isInviteUserRequestBOValid != null && !isInviteUserRequestBOValid) {
            return;
        }

        UserBO userBO = new UserBO();

        if (inviteUserRequestBO.getIsUserCreationRequired() != null &&
            inviteUserRequestBO.getIsUserCreationRequired()) {
            userBO = createUser(inviteUserRequestBO, clientSession);
            inviteUserRequestBO.setUserId(userBO.getId());
        }

        if (inviteUserRequestBO.getIsUserOrgMappingCreationRequired() != null &&
                inviteUserRequestBO.getIsUserOrgMappingCreationRequired()) {
            createUserOrgMapping(inviteUserRequestBO, clientSession);
        }

        if (inviteUserRequestBO.getIsUserProjectMappingCreationRequired() != null &&
            inviteUserRequestBO.getIsUserProjectMappingCreationRequired()) {
            createUserProjectMapping(inviteUserRequestBO, clientSession);
        }

        if (inviteUserRequestBO.getIsUserCreationRequired() != null &&
                inviteUserRequestBO.getIsUserCreationRequired()) {
            sendInvitationEmail(userBO.getPlainTextPassword(), userBO.getEmail(), inviteUserRequestBO.getUserBO());
        }
    }

    private void createUserProjectMapping(InviteUserRequestBO inviteUserRequestBO, ClientSession clientSession) throws Exception {
        logger.info("Inside createUserProjectMapping");

        UserProjectMapping userProjectMapping = new UserProjectMapping();

        userProjectMapping.setId(new ObjectId());
        userProjectMapping.setProjectId(new ObjectId(inviteUserRequestBO.getProjectId()));
        userProjectMapping.setUserId(new ObjectId(inviteUserRequestBO.getUserId()));

        userProjectMappingDAO.createUserProjectMapping(userProjectMapping, clientSession);
    }

    private void createUserOrgMapping(InviteUserRequestBO inviteUserRequestBO, ClientSession clientSession) throws Exception {
        logger.info("Inside createUserOrgMapping");

        UserOrganisationMapping userOrganisationMapping = new UserOrganisationMapping();

        userOrganisationMapping.setId(new ObjectId());
        userOrganisationMapping.setOrganisationId(new ObjectId(inviteUserRequestBO.getOrganisationId()));
        userOrganisationMapping.setUserId(new ObjectId(inviteUserRequestBO.getUserId()));

        userOrganisationMappingDAO.createUserOrganisationMapping(userOrganisationMapping, clientSession);
    }

    private UserBO createUser(InviteUserRequestBO inviteUserRequestBO, ClientSession clientSession) throws Exception {
        logger.info("Inside createUser");

        User user = new User();
        UserBO userBO = new UserBO();

        user.setId(new ObjectId());
        user.setName(inviteUserRequestBO.getName());
        user.setEmail(inviteUserRequestBO.getEmail());
        userBO.setPlainTextPassword(UUID.randomUUID().toString());
        user.setPassword(hashService.getSha256HexString(userBO.getPlainTextPassword()));
        user.setRole(Role.SENDER.name());

        BeanUtils.copyProperties(user, userBO);
        userBO.setId(user.getId().toHexString());

        userDAO.createUser(user, clientSession);

        return userBO;
    }

    private Boolean validateInviteUserRequestBO(InviteUserRequestBO inviteUserRequestBO) {
        logger.info("Inside validateInviteUserRequestBO");

        if (inviteUserRequestBO == null ||
                StringUtils.isEmpty(inviteUserRequestBO.getEmail()) ||
                StringUtils.isEmpty(inviteUserRequestBO.getName()) ||
                StringUtils.isEmpty(inviteUserRequestBO.getOrganisationId()) ||
                StringUtils.isEmpty(inviteUserRequestBO.getProjectId()) ||
                inviteUserRequestBO.getUserBO() == null) {
            return false;
        }

        return true;
    }
}
