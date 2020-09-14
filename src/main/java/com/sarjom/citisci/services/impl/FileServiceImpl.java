package com.sarjom.citisci.services.impl;

import com.sarjom.citisci.bos.FileBO;
import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.db.mongo.daos.IFileDAO;
import com.sarjom.citisci.db.mongo.daos.IProjectDAO;
import com.sarjom.citisci.db.mongo.daos.IUserDAO;
import com.sarjom.citisci.db.mongo.daos.IUserProjectMappingDAO;
import com.sarjom.citisci.dtos.*;
import com.sarjom.citisci.entities.*;
import com.sarjom.citisci.enums.FileType;
import com.sarjom.citisci.enums.Role;
import com.sarjom.citisci.services.IFileService;
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
public class FileServiceImpl implements IFileService {
    private static Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Autowired
    IFileDAO fileDAO;
    @Autowired
    IProjectDAO projectDAO;
    @Autowired
    IUserProjectMappingDAO userProjectMappingDAO;
    @Autowired
    IUserDAO userDAO;

    @Autowired
    IAwsSesService awsSesService;

    @Value("${invitation.sender.email}")
    String senderEmail;

    @Override
    public CreateFileResponseDTO createFile(CreateFileRequestDTO createFileRequestDTO, UserBO userBO) throws Exception {
        logger.info("Inside createFile");

        validateCreateFileRequestDTO(createFileRequestDTO, userBO);

        File file = populateFile(createFileRequestDTO, userBO);

        fileDAO.createFile(file, null);

        Map<String, UserBO> userIdToUserBOMap = new HashMap<>();

        userIdToUserBOMap.put(userBO.getId(), userBO);

        FileBO fileBO = convertToFileBO(file, userIdToUserBOMap);

        CreateFileResponseDTO createFileResponseDTO = new CreateFileResponseDTO();
        createFileResponseDTO.setCreatedFile(fileBO);

        return createFileResponseDTO;
    }

    @Override
    public DownloadFilesResponseDTO downloadFiles(String projectId, UserBO userBO) throws Exception {
        logger.info("Inside downloadFiles");

        if (StringUtils.isEmpty(projectId)) {
            throw new Exception("Invalid request");
        }

        Project project = checkThatProjectExists(new ObjectId(projectId));
        checkThatUserAndProjectAreLinked(new ObjectId(projectId),
                new ObjectId(userBO.getId()));

        DownloadFilesResponseDTO downloadFilesResponseDTO = new DownloadFilesResponseDTO();
        downloadFilesResponseDTO.setStatus("Project file links are being mailed");
        downloadFilesResponseDTO.setProject(project);

        return downloadFilesResponseDTO;
    }

    @Override
    @Async
    public void sendFileLinks(Project project, UserBO userBO) throws Exception {
        logger.info("Inside sendFileLinks");

        String projectId = project.getId().toHexString();

        List<File> files = fileDAO.fetchByProjectId(new ObjectId(projectId));

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

        String subject = "Dataset links for project " + project.getName();

        String message = "";

        for (FileBO fileBO: fileBOs) {
            message = message + "File name: " + fileBO.getName() + "\n"
                    + "File link: " + fileBO.getFileLink() + "\n"
                    + "File uploaded by: " + fileBO.getUploadedByUser().getName() + "\n\n";
        }

        awsSesService.sendEmail(senderEmail, userBO.getEmail(), subject, message);
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

    private File populateFile(CreateFileRequestDTO createFileRequestDTO, UserBO userBO) {
        logger.info("Inside populateFile");

        File file = new File();

        BeanUtils.copyProperties(createFileRequestDTO, file);

        file.setId(new ObjectId());
        file.setProjectId(new ObjectId(createFileRequestDTO.getProjectId()));
        file.setUploadedByUserId(new ObjectId(userBO.getId()));

        return file;
    }

    private void validateCreateFileRequestDTO(CreateFileRequestDTO createFileRequestDTO, UserBO userBO) throws Exception {
        logger.info("Inside validateCreateFileRequestDTO");

        if (createFileRequestDTO == null ||
                StringUtils.isEmpty(createFileRequestDTO.getProjectId()) ||
                StringUtils.isEmpty(createFileRequestDTO.getName()) ||
                StringUtils.isEmpty(createFileRequestDTO.getFileLink()) ||
                StringUtils.isEmpty(createFileRequestDTO.getLicense()) ||
                StringUtils.isEmpty(createFileRequestDTO.getFileType())) {
            throw new Exception("Invalid request");
        }

        checkThatProjectExists(new ObjectId(createFileRequestDTO.getProjectId()));
        checkThatUserAndProjectAreLinked(new ObjectId(createFileRequestDTO.getProjectId()),
                new ObjectId(userBO.getId()));
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
    public FetchFilesResponseDTO fetchFilesForProject(FetchFilesRequestDTO fetchFilesRequestDTO, UserBO userBO) throws Exception {
        logger.info("Inside fetchFilesForProject");

        FetchFilesResponseDTO fetchFilesResponseDTO = new FetchFilesResponseDTO();

        validateFetchFilesRequestDTO(fetchFilesRequestDTO, userBO);

        List<File> files = fileDAO.fetchByProjectId(new ObjectId(fetchFilesRequestDTO.getProjectId()));

        if (CollectionUtils.isEmpty(files)) {
            return fetchFilesResponseDTO;
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

        fetchFilesResponseDTO.setFiles(fileBOs);

        return fetchFilesResponseDTO;
    }

    private void validateFetchFilesRequestDTO(FetchFilesRequestDTO fetchFilesRequestDTO, UserBO userBO) throws Exception {
        logger.info("Inside validateFetchFilesRequestDTO");

        if (fetchFilesRequestDTO == null ||
                StringUtils.isEmpty(fetchFilesRequestDTO.getProjectId())) {
            throw new Exception("Invalid request");
        }

        if (userBO == null) {
            throw new Exception("Please login again");
        }

        if (userBO.getRole() == null || !userBO.getRole().name().equalsIgnoreCase(Role.COLLECTOR.name())) {
            throw new Exception("User must be a collector to view project files");
        }

        checkThatProjectExists(new ObjectId(fetchFilesRequestDTO.getProjectId()));
        checkThatUserAndProjectAreLinked(new ObjectId(fetchFilesRequestDTO.getProjectId()),
                new ObjectId(userBO.getId()));
    }
}