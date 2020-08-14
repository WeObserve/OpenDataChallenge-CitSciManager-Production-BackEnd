package com.sarjom.citisci.services.transactional.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.mongodb.MongoClient;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.db.mongo.daos.IUserDAO;
import com.sarjom.citisci.dtos.CreateUserResponseDTO;
import com.sarjom.citisci.entities.User;
import com.sarjom.citisci.services.transactional.IUserTransService;
import com.sarjom.citisci.services.utilities.IAwsSesService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

        sendInvitationEmail(userBO.getPlainTextPassword(), userBO.getEmail());

        userBO.setPlainTextPassword(null);
        userBO.setPassword(null);

        CreateUserResponseDTO createUserResponseDTO = new CreateUserResponseDTO();
        createUserResponseDTO.setCreatedUser(userBO);

        return createUserResponseDTO;
    }

    private void sendInvitationEmail(String plainTextPassword, String recipientEmail) throws Exception {
        logger.info("Inside sendInvitationEmail");

        Algorithm algorithm = Algorithm.HMAC256(masterKey);

        String jwtEncodedPassword = JWT.create().withClaim("password", plainTextPassword).sign(algorithm);

        String message = invitationEmailLinkEndpoint + jwtEncodedPassword;
        String subject = "Invitation from Greendubs";

        awsSesService.sendEmail(invitationSenderEmail, recipientEmail, subject, message);
    }
}
