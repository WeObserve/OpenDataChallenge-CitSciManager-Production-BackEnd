package com.sarjom.citisci.services.impl;

import com.sarjom.citisci.bos.*;
import com.sarjom.citisci.db.mongo.daos.*;
import com.sarjom.citisci.dtos.*;
import com.sarjom.citisci.entities.*;
import com.sarjom.citisci.enums.FileType;
import com.sarjom.citisci.enums.ProjectType;
import com.sarjom.citisci.enums.Role;
import com.sarjom.citisci.services.IDatastoryService;
import com.sarjom.citisci.services.utilities.IAwsSesService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class DatastoryServiceImpl implements IDatastoryService {
    private static Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Autowired
    IFileDAO fileDAO;
    @Autowired
    IProjectDAO projectDAO;
    @Autowired
    IUserProjectMappingDAO userProjectMappingDAO;
    @Autowired
    IDatastoryDAO datastoryDAO;
    @Autowired
    IUserDAO userDAO;

    @Autowired
    IAwsSesService awsSesService;

    @Value("${published.datastory.email.link.endpoint}")
    String publishedDatastoryEmailLinkEndpoint;

    @Value("${invitation.sender.email}")
    String senderEmail;

    @Override
    public CreateDatastoryResponseDTO createDatastory(CreateDatastoryRequestDTO createDatastoryRequestDTO, UserBO userBO) throws Exception {
        logger.info("Inside createDatastory");

        Project project = validateCreateDatastoryRequestDTO(createDatastoryRequestDTO, userBO);

        Datastory datastory = populateDatastory(createDatastoryRequestDTO, userBO);

        datastoryDAO.createDatastory(datastory, null);

        DatastoryBO datastoryBO = convertToDatastoryBO(datastory);

        populateDatastoryBO(datastoryBO, userBO, convertToProjectBO(project));

        CreateDatastoryResponseDTO createDatastoryResponseDTO = new CreateDatastoryResponseDTO();
        createDatastoryResponseDTO.setCreatedDatastory(datastoryBO);

        return createDatastoryResponseDTO;
    }

    @Override
    public ViewDatastoryResponseDTO viewDatastory(String datastoryId, UserBO userBO) throws Exception {
        logger.info("Inside viewDatastory");

        if (StringUtils.isEmpty(datastoryId)) {
            throw new Exception("Invalid request");
        }

        //Doesn't check anything other than whether datastory exists and project exists and returns datastory and project
        ViewDatastoryRequestValidationResponseBO viewDatastoryRequestValidationResponseBO =
                checkIfDatastoryExistsAndIfProjectAndUserAreLinked(new ObjectId(datastoryId),
                   null);

        DatastoryBO datastoryBO = convertToDatastoryBO(
                viewDatastoryRequestValidationResponseBO.getDatastory());

        User createdByUser = userDAO.getUsersByIds(Arrays.asList(viewDatastoryRequestValidationResponseBO.getDatastory().createdByUserId)).get(0);

        UserBO createdByUserBO = convertToUserBO(createdByUser);

        populateDatastoryBO(datastoryBO, createdByUserBO,
                convertToProjectBO(viewDatastoryRequestValidationResponseBO.getProject()));

        ViewDatastoryResponseDTO viewDatastoryResponseDTO = new ViewDatastoryResponseDTO();
        viewDatastoryResponseDTO.setDatastory(datastoryBO);

        return viewDatastoryResponseDTO;
    }

    @Async
    @Override
    public void sendDatastoryPublishedEmails(DatastoryBO createdDatastory) {
        logger.info("Inside sendDatastoryPublishedEmails");

        try {
            String subject = "Datastory Published";
            String message = "Datastory name: " + createdDatastory.getName() + "\n"
                    + "Project name: " + createdDatastory.getProject().getName() + "\n"
                    + "Published by: " + createdDatastory.getCreatedByUser().getName() + "\n"
                    + "Datastory link: " + publishedDatastoryEmailLinkEndpoint + createdDatastory.getId();

            List<String> recipientEmails = new ArrayList<>();

            recipientEmails.add(createdDatastory.getCreatedByUser().getEmail());

            if (!CollectionUtils.isEmpty(createdDatastory.getFiles())) {
                for (FileBO fileBO : createdDatastory.getFiles()) {
                    if (!CollectionUtils.isEmpty(recipientEmails) &&
                            recipientEmails.contains(fileBO.getUploadedByUser().getEmail())) {
                        continue;
                    }

                    recipientEmails.add(fileBO.getUploadedByUser().getEmail());
                }
            }

            for (String email: recipientEmails) {
                try {
                    awsSesService.sendEmail(senderEmail, email, "Datastory Published", message);
                } catch (Exception e) {
                    logger.error("Error while sending email to {}: {}", email, e);
                }
            }
        } catch (Exception e) {
            logger.error("Error while trying to send datastory published emails: ", e);
        }
    }

    private ProjectBO convertToProjectBO(Project project) {
        logger.info("Inside convertToProjectBO");

        ProjectBO projectBO = new ProjectBO();

        BeanUtils.copyProperties(project, projectBO);

        projectBO.setId(project.getId().toHexString());
        projectBO.setOrganisationId(project.getOrganisationId().toHexString());
        projectBO.setCreatedByUserId(project.getCreatedByUserId().toHexString());
        projectBO.setProjectType(ProjectType.valueOf(project.getProjectType()));

        return projectBO;
    }

    private ViewDatastoryRequestValidationResponseBO checkIfDatastoryExistsAndIfProjectAndUserAreLinked(ObjectId datastoryId,
                                                                                                        ObjectId userId) throws Exception {
        logger.info("Inside checkIfDatastoryExistsAndIfProjectAndUserAreLinked");

        List<Datastory> datastories = datastoryDAO.getByIds(Arrays.asList(datastoryId));

        if (CollectionUtils.isEmpty(datastories)) {
            throw new Exception("No datastory exists");
        }

        Datastory datastory = datastories.get(0);

        Project project = checkThatProjectExists(datastory.getProjectId());
        //checkThatUserAndProjectAreLinked(datastory.getProjectId(), userId);

        ViewDatastoryRequestValidationResponseBO viewDatastoryRequestValidationResponseBO = new ViewDatastoryRequestValidationResponseBO();

        viewDatastoryRequestValidationResponseBO.setDatastory(datastory);
        viewDatastoryRequestValidationResponseBO.setProject(project);

        return viewDatastoryRequestValidationResponseBO;
    }

    private void populateDatastoryBO(DatastoryBO datastoryBO, UserBO userBO,
                                     ProjectBO projectBO) throws Exception {
        logger.info("inside populateDatastoryBO");

        datastoryBO.setCreatedByUser(userBO);

        List<File> files = fileDAO.fetchByProjectId(new ObjectId(datastoryBO.getProjectId()));

        if (CollectionUtils.isEmpty(files)) {
            return;
        }

        List<ObjectId> userIds = new ArrayList<>();

        for (File file: files) {
            if (!CollectionUtils.isEmpty(userIds) &&
                userIds.contains(file.getUploadedByUserId())) {
                continue;
            }

            userIds.add(file.getUploadedByUserId());
        }

        List<User> users = userDAO.getUsersByIds(userIds);

        Map<String, UserBO> userIdToUserBOMap = new HashMap<>();

        for (User user: users) {
            userIdToUserBOMap.put(user.getId().toHexString(), convertToUserBO(user));
        }

        List<FileBO> fileBOs = new ArrayList<>();

        for (File file: files) {
            fileBOs.add(convertToFileBO(file, userIdToUserBOMap));
        }

        datastoryBO.setFiles(fileBOs);
        datastoryBO.setProject(projectBO);
    }

    private FileBO convertToFileBO(File file, Map<String, UserBO> userIdToUserBOMap) {
        logger.info("Inside convertToFileBO");

        FileBO fileBO = new FileBO();

        BeanUtils.copyProperties(file, fileBO);

        fileBO.setId(file.getId().toHexString());
        fileBO.setProjectId(file.getProjectId().toHexString());
        fileBO.setFileType(FileType.valueOf(file.getFileType()));
        fileBO.setUploadedByUserId(file.getUploadedByUserId().toHexString());
        fileBO.setUploadedByUser(userIdToUserBOMap.get(fileBO.getUploadedByUserId()));

        return fileBO;
    }

    private UserBO convertToUserBO(User user) {
        logger.info("Inside convertToUserBO");

        UserBO userBO = new UserBO();
        userBO.setId(user.getId().toHexString());
        userBO.setEmail(user.getEmail());
        userBO.setName(user.getName());
        userBO.setRole(Role.valueOf(user.getRole()));

        return userBO;
    }

    private DatastoryBO convertToDatastoryBO(Datastory datastory) {
        logger.info("Inside convertToDatastoryBO");

        DatastoryBO datastoryBO = new DatastoryBO();

        BeanUtils.copyProperties(datastory, datastoryBO);

        datastoryBO.setId(datastory.getId().toHexString());
        datastoryBO.setProjectId(datastory.getProjectId().toHexString());
        datastoryBO.setCreatedByUserId(datastory.getCreatedByUserId().toHexString());

        return datastoryBO;
    }

    private Datastory populateDatastory(CreateDatastoryRequestDTO createDatastoryRequestDTO, UserBO userBO) {
        logger.info("Inside populateDatastory");

        Datastory datastory = new Datastory();

        BeanUtils.copyProperties(createDatastoryRequestDTO, datastory);

        datastory.setId(new ObjectId());
        datastory.setProjectId(new ObjectId(createDatastoryRequestDTO.getProjectId()));
        datastory.setCreatedByUserId(new ObjectId(userBO.getId()));
        datastory.setIsDraft(createDatastoryRequestDTO.getIsDraft());

        return datastory;
    }

    private Project validateCreateDatastoryRequestDTO(CreateDatastoryRequestDTO createDatastoryRequestDTO, UserBO userBO) throws Exception {
        logger.info("Inside validateCreateDatastoryRequestDTO");

        if (createDatastoryRequestDTO == null ||
                StringUtils.isEmpty(createDatastoryRequestDTO.getProjectId()) ||
                StringUtils.isEmpty(createDatastoryRequestDTO.getName()) ||
                StringUtils.isEmpty(createDatastoryRequestDTO.getType()) ||
                createDatastoryRequestDTO.getIsDraft() == null) {
            throw new Exception("Invalid request");
        }

        if (userBO == null || userBO.getRole() == null || !userBO.getRole().equals(Role.COLLECTOR)) {
            throw new Exception("This user can't create datastory");
        }

        Project project = checkThatProjectExists(new ObjectId(createDatastoryRequestDTO.getProjectId()));
        checkThatUserAndProjectAreLinked(new ObjectId(createDatastoryRequestDTO.getProjectId()),
                new ObjectId(userBO.getId()));

        return project;
    }

    private void checkThatUserAndProjectAreLinked(ObjectId projectId, ObjectId userId) throws Exception {
        logger.info("Inside checkThatUserAndProjectAreLinked");

        List<UserProjectMapping> userProjectMappings = userProjectMappingDAO.fetchByProjectIdAndUserIds(
                projectId, Arrays.asList(userId));

        if (CollectionUtils.isEmpty(userProjectMappings)) {
            throw new Exception("User is not linked with this project");
        }
    }

    private Project checkThatProjectExists(ObjectId projectId) throws Exception {
        logger.info("Inside checkThatProjectExists");

        List<Project> projects = projectDAO.fetchByIds(Arrays.asList(projectId));

        if (CollectionUtils.isEmpty(projects)) {
            throw new Exception("No project found with this id");
        }

        return projects.get(0);
    }

    @Override
    public FetchDatastoryResponseDTO fetchDatastoriesForProject(String projectId, UserBO userBO) throws Exception {
        logger.info("Inside fetchDatastoriesForProject");

        Project project = validateFetchDatastoriesRequest(projectId, userBO);

        List<Datastory> datastories = datastoryDAO.getByProjectIds(Arrays.asList(project.getId()));

        if (CollectionUtils.isEmpty(datastories)) {
            return new FetchDatastoryResponseDTO();
        }

        List<DatastoryBO> datastoryBOs = convertToDatastoryBOs(datastories);

        FetchDatastoryResponseDTO fetchDatastoryResponseDTO = new FetchDatastoryResponseDTO();
        fetchDatastoryResponseDTO.setDatastories(datastoryBOs);

        return fetchDatastoryResponseDTO;
    }

    private List<DatastoryBO> convertToDatastoryBOs(List<Datastory> datastories) {
        logger.info("Inside convertToDatastoryBOs");

        List<DatastoryBO> datastoryBOs = new ArrayList<>();

        for (Datastory datastory: datastories) {
            if (datastory == null) {
                continue;
            }

            datastoryBOs.add(convertToDatastoryBO(datastory));
        }

        return datastoryBOs;
    }

    private Project validateFetchDatastoriesRequest(String projectId, UserBO userBO) throws Exception {
        logger.info("Inside validateFetchDatastoriesRequest");

        if (StringUtils.isEmpty(projectId)) {
            throw new Exception("projectId cannot be empty");
        }

        if (userBO == null) {
            throw new Exception("Please login again");
        }

        if (userBO== null || !userBO.getRole().name().equalsIgnoreCase(Role.COLLECTOR.name())) {
            throw new Exception("This user is not a COLLECTOR");
        }

        Project project = checkThatProjectExists(new ObjectId(projectId));
        checkThatUserAndProjectAreLinked(new ObjectId(projectId),
                new ObjectId(userBO.getId()));

        return project;
    }

    @Override
    public PublishDraftDatastoryResponseDTO convertDraftToPublishedDatastory(String datastoryId, UserBO userBO) throws Exception {
        logger.info("Inside convertDraftToPublishedDatastory");

        Datastory datastory = validateConvertDraftToPublishedDatastoryRequest(datastoryId, userBO);

        datastoryDAO.convertDraftToPublishedDatastory(new ObjectId(datastoryId), null);

        List<Project> projects = projectDAO.fetchByIds(Arrays.asList(datastory.getProjectId()));

        if (CollectionUtils.isEmpty(projects)) {
            throw new Exception("Datastory not linked to any project");
        }

        Project project = projects.get(0);

        DatastoryBO datastoryBO = convertToDatastoryBO(datastory);

        populateDatastoryBO(datastoryBO, userBO, convertToProjectBO(project));

        PublishDraftDatastoryResponseDTO publishDraftDatastoryResponseDTO = new PublishDraftDatastoryResponseDTO();
        publishDraftDatastoryResponseDTO.setPublishedDatastory(datastoryBO);

        return publishDraftDatastoryResponseDTO;
    }

    private Datastory validateConvertDraftToPublishedDatastoryRequest(String datastoryId, UserBO userBO) throws Exception {
        logger.info("Inside validateConvertDraftToPublishedDatastoryRequest");

        if (StringUtils.isEmpty(datastoryId)) {
            throw new Exception("Datastory id cannot be empty");
        }

        if (userBO == null) {
            throw new Exception("Please login again");
        }

        if (userBO.getRole() == null || !userBO.getRole().name().equalsIgnoreCase(Role.COLLECTOR.name())) {
            throw new Exception("This user is not a COLLECTOR");
        }

        List<Datastory> datastories = datastoryDAO.getByIds(Arrays.asList(new ObjectId(datastoryId)));

        if (CollectionUtils.isEmpty(datastories)) {
            throw new Exception("No datastory exists");
        }

        Datastory datastory = datastories.get(0);

        if (!userBO.getId().equalsIgnoreCase(datastory.getCreatedByUserId().toHexString())) {
            throw new Exception("This user can't publish this datastory");
        }

        return datastory;
    }
}
