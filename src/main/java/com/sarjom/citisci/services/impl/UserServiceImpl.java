package com.sarjom.citisci.services.impl;

import com.sarjom.citisci.bos.InviteUserRequestBO;
import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.db.mongo.daos.*;
import com.sarjom.citisci.dtos.CreateUserRequestDTO;
import com.sarjom.citisci.dtos.CreateUserResponseDTO;
import com.sarjom.citisci.dtos.InviteUserRequestDTO;
import com.sarjom.citisci.dtos.InviteUserResponseDTO;
import com.sarjom.citisci.entities.*;
import com.sarjom.citisci.services.IUserService;
import com.sarjom.citisci.services.transactional.IUserTransService;
import com.sarjom.citisci.services.utilities.IAwsS3Service;
import com.sarjom.citisci.services.utilities.ICsvService;
import com.sarjom.citisci.services.utilities.IHashService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements IUserService {
    private static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired IUserDAO userDAO;
    @Autowired IHashService hashService;
    @Autowired
    IUserTransService userTransService;
    @Autowired
    IOrganisationDAO organisationDAO;
    @Autowired
    IProjectDAO projectDAO;
    @Autowired
    IAwsS3Service awsS3Service;
    @Autowired
    ICsvService csvService;
    @Autowired
    IUserOrganisationMappingDAO userOrganisationMappingDAO;
    @Autowired
    IUserProjectMappingDAO userProjectMappingDAO;

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

    @Override
    public InviteUserResponseDTO inviteUser(InviteUserRequestDTO inviteUserRequestDTO, UserBO userBO) throws Exception {
        logger.info("Inside inviteUser");

        validateInviteUserRequestDTO(inviteUserRequestDTO);

        InviteUserResponseDTO inviteUserResponseDTO = new InviteUserResponseDTO();
        inviteUserResponseDTO.setStatus("Successfully invited");

        return inviteUserResponseDTO;
    }


    @Override
    @Async
    public void processInviteUsers(InviteUserRequestDTO inviteUserRequestDTO, UserBO userBO) throws Exception {
        logger.info("Inside processInviteUsers");

        String bucketName = inviteUserRequestDTO.getBucketName();
        String objectKey = inviteUserRequestDTO.getUserInvitationFileS3Key();

        File file = awsS3Service.downloadFileFromS3(bucketName, objectKey);

        List<Map<String, String>> userEmailAndNameMapList = csvService.convertCsvToListOfMap(file);

        Map<String, InviteUserRequestBO> emailToInviteUserRequestBOMap = populateEmailToInviteUserRequestBOMap(
                userEmailAndNameMapList, inviteUserRequestDTO, userBO);

        for (Map.Entry<String, InviteUserRequestBO> entry: emailToInviteUserRequestBOMap.entrySet()) {
            try {
                userTransService.inviteUser(entry.getValue(), true);
            } catch (Exception e) {
                logger.error("Error while inviting {}: ", entry.getKey(), e);
            }
        }

        file.delete();
    }

    private Map<String, InviteUserRequestBO> populateEmailToInviteUserRequestBOMap(
            List<Map<String, String>> userEmailAndNameMapList, InviteUserRequestDTO inviteUserRequestDTO,
            UserBO userBO) throws Exception {
        logger.info("Inside populateEmailToInviteUserRequestBOMap");

        if (CollectionUtils.isEmpty(userEmailAndNameMapList)) {
            return null;
        }

        Map<String, String> emailToNameMap = new HashMap<>();

        for (Map<String, String> userEmailAndNameMap: userEmailAndNameMapList) {
            if (userEmailAndNameMap == null ||
                !userEmailAndNameMap.containsKey("email") ||
                !userEmailAndNameMap.containsKey("name") ||
                StringUtils.isEmpty(userEmailAndNameMap.get("email")) ||
                StringUtils.isEmpty(userEmailAndNameMap.get("name")) ||
                    (!CollectionUtils.isEmpty(emailToNameMap) &&
                            emailToNameMap.containsKey(userEmailAndNameMap.get("email")))) {
                continue;
            }

            emailToNameMap.put(userEmailAndNameMap.get("email"),
                    userEmailAndNameMap.get("name"));
        }

        if (CollectionUtils.isEmpty(emailToNameMap)) {
            return null;
        }

        List<User> users = userDAO.getUsersByEmails(new ArrayList<>(emailToNameMap.keySet()));
        List<ObjectId> userIds = new ArrayList<>();
        List<UserOrganisationMapping> userOrganisationMappings = new ArrayList<>();
        List<UserProjectMapping> userProjectMappings = new ArrayList<>();
        List<ObjectId> userIdsAlreadyLinkedToOrg = new ArrayList<>();
        List<ObjectId> userIdsAlreadyLinkedToProject = new ArrayList<>();
        Map<String, User> emailToUserMap = new HashMap<>();

        if (!CollectionUtils.isEmpty(users)) {
            userIds = users.stream().map(User::getId).collect(Collectors.toList());
            userOrganisationMappings = userOrganisationMappingDAO.fetchByOrgIdAndUserIds(
                    new ObjectId(inviteUserRequestDTO.getOrganisationId()), userIds);
            userProjectMappings = userProjectMappingDAO.fetchByProjectIdAndUserIds(
                    new ObjectId(inviteUserRequestDTO.getProjectId()), userIds);
            emailToUserMap = users.stream().collect(Collectors.toMap(User::getEmail,
                    user -> user, (oldv, newv) -> oldv));
        }

        if (!CollectionUtils.isEmpty(userOrganisationMappings)) {
            userIdsAlreadyLinkedToOrg = userOrganisationMappings
                    .stream().map(UserOrganisationMapping::getUserId).collect(Collectors.toList());
        }

        if (!CollectionUtils.isEmpty(userProjectMappings)) {
            userIdsAlreadyLinkedToProject = userProjectMappings
                    .stream().map(UserProjectMapping::getUserId).collect(Collectors.toList());
        }

        Map<String, InviteUserRequestBO> emailToInviteUserRequestBOMap = new HashMap<>();

        for (Map.Entry<String, String> entry: emailToNameMap.entrySet()) {
            InviteUserRequestBO inviteUserRequestBO = new InviteUserRequestBO();

            inviteUserRequestBO.setEmail(entry.getKey());
            inviteUserRequestBO.setName(entry.getValue());
            inviteUserRequestBO.setOrganisationId(inviteUserRequestDTO.getOrganisationId());
            inviteUserRequestBO.setProjectId(inviteUserRequestDTO.getProjectId());
            inviteUserRequestBO.setUserBO(userBO);

            if (CollectionUtils.isEmpty(emailToUserMap) ||
                !emailToUserMap.containsKey(entry.getKey())) {
                inviteUserRequestBO.setIsUserCreationRequired(true);
                inviteUserRequestBO.setIsUserOrgMappingCreationRequired(true);
                inviteUserRequestBO.setIsUserProjectMappingCreationRequired(true);

                emailToInviteUserRequestBOMap.put(entry.getKey(), inviteUserRequestBO);
                continue;
            }

            User user = emailToUserMap.get(entry.getKey());

            inviteUserRequestBO.setUserId(user.getId().toHexString());

            if (!CollectionUtils.isEmpty(userIdsAlreadyLinkedToOrg) &&
                userIdsAlreadyLinkedToOrg.contains(user.getId())) {
                inviteUserRequestBO.setIsUserOrgMappingCreationRequired(false);
            } else {
                inviteUserRequestBO.setIsUserOrgMappingCreationRequired(true);
            }

            if (!CollectionUtils.isEmpty(userIdsAlreadyLinkedToProject) &&
                    userIdsAlreadyLinkedToProject.contains(user.getId())) {
                inviteUserRequestBO.setIsUserProjectMappingCreationRequired(false);
            } else {
                inviteUserRequestBO.setIsUserProjectMappingCreationRequired(true);
            }

            emailToInviteUserRequestBOMap.put(entry.getKey(), inviteUserRequestBO);
        }

        return emailToInviteUserRequestBOMap;
    }

    private void validateInviteUserRequestDTO(InviteUserRequestDTO inviteUserRequestDTO) throws Exception {
        logger.info("Inside validateInviteUserRequestDTO");

        if (inviteUserRequestDTO == null ||
            StringUtils.isEmpty(inviteUserRequestDTO.getOrganisationId()) ||
            StringUtils.isEmpty(inviteUserRequestDTO.getProjectId()) ||
            StringUtils.isEmpty(inviteUserRequestDTO.getBucketName()) ||
            StringUtils.isEmpty(inviteUserRequestDTO.getUserInvitationFileS3Key())) {
            throw new Exception("Invalid request");
        }

        checkIfOrganisationExists(new ObjectId(inviteUserRequestDTO.getOrganisationId()));
        checkIfProjectExistsAndIsLinkedToThisOrg(new ObjectId(inviteUserRequestDTO.getOrganisationId()),
                new ObjectId(inviteUserRequestDTO.getProjectId()));
    }

    private void checkIfProjectExistsAndIsLinkedToThisOrg(ObjectId orgId, ObjectId projectId) throws Exception {
        logger.info("Inside checkIfProjectExistsAndIsLinkedToThisOrg");

        List<Project> projects = projectDAO.fetchByIds(Arrays.asList(projectId));

        if (CollectionUtils.isEmpty(projects)) {
            throw new Exception("No project with this id");
        }

        Project project = projects.get(0);

        if (!project.getOrganisationId().toHexString().equalsIgnoreCase(orgId.toHexString())) {
            throw new Exception("This project is not linked to this organisation");
        }
    }

    private void checkIfOrganisationExists(ObjectId orgId) throws Exception {
        logger.info("inside checkIfOrganisationExists");

        List<Organisation> organisations = organisationDAO.fetchByIds(Arrays.asList(orgId));

        if (CollectionUtils.isEmpty(organisations)) {
            throw new Exception("No organisation with given id");
        }
    }
}