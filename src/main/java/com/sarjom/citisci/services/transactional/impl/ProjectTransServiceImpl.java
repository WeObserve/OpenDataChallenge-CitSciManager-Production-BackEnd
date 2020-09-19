package com.sarjom.citisci.services.transactional.impl;

import com.mongodb.MongoClient;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.sarjom.citisci.bos.ProjectBO;
import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.db.mongo.daos.*;
import com.sarjom.citisci.dtos.CreateProjectResponseDTO;
import com.sarjom.citisci.dtos.CreateUserResponseDTO;
import com.sarjom.citisci.dtos.DeleteProjectResponseDTO;
import com.sarjom.citisci.entities.Project;
import com.sarjom.citisci.entities.User;
import com.sarjom.citisci.entities.UserProjectMapping;
import com.sarjom.citisci.services.transactional.IProjectTransService;
import com.sarjom.citisci.services.utilities.IAwsSesService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectTransServiceImpl implements IProjectTransService {
    private static Logger logger = LoggerFactory.getLogger(ProjectTransServiceImpl.class);

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
    IProjectDAO projectDAO;

    @Autowired
    IUserProjectMappingDAO userProjectMappingDAO;

    @Autowired
    IDatastoryDAO datastoryDAO;

    @Autowired
    IFileDAO fileDAO;

    @Override
    public CreateProjectResponseDTO createProject(ProjectBO projectBO, Boolean useTxn) throws Exception {
        logger.info("Inside createProject");

        if (useTxn == null || !useTxn) {
            return createProjectWithoutTxn(projectBO, null);
        }

        return createProjectWithTxn(projectBO);
    }

    private CreateProjectResponseDTO createProjectWithTxn(ProjectBO projectBO) throws Exception {
        logger.info("Inside createProjectWithTxn");

        ClientSession clientSession = mongoClient.startSession();

        try {
            clientSession.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());

            CreateProjectResponseDTO createProjectResponseDTO = createProjectWithoutTxn(projectBO, clientSession);

            clientSession.commitTransaction();
            clientSession.close();

            return createProjectResponseDTO;
        } catch (Exception e) {
            clientSession.abortTransaction();
            clientSession.close();
            throw e;
        }
    }

    private CreateProjectResponseDTO createProjectWithoutTxn(ProjectBO projectBO, ClientSession clientSession) throws Exception {
        logger.info("Inside createProjectWithoutTxn");

        Project project = new Project();
        UserProjectMapping userProjectMapping = new UserProjectMapping();

        BeanUtils.copyProperties(projectBO, project);

        project.setId(new ObjectId(projectBO.getId()));
        project.setOrganisationId(new ObjectId(projectBO.getOrganisationId()));
        project.setCreatedByUserId(new ObjectId(projectBO.getCreatedByUserId()));
        project.setProjectType(projectBO.getProjectType().name());

        userProjectMapping.setId(new ObjectId());
        userProjectMapping.setProjectId(project.getId());
        userProjectMapping.setUserId(project.getCreatedByUserId());

        projectDAO.createProject(project, clientSession);
        userProjectMappingDAO.createUserProjectMapping(userProjectMapping, clientSession);

        CreateProjectResponseDTO createProjectResponseDTO = new CreateProjectResponseDTO();
        createProjectResponseDTO.setCreatedProject(projectBO);

        return createProjectResponseDTO;
    }

    @Override
    public DeleteProjectResponseDTO deleteProject(List<ObjectId> projectIds, Boolean useTxn) throws Exception {
        logger.info("Inside deleteProject");

        if (useTxn == null || !useTxn) {
            return deleteProjectWithoutTxn(projectIds, null);
        }

        return deleteProjectWithTxn(projectIds);
    }

    private DeleteProjectResponseDTO deleteProjectWithTxn(List<ObjectId> projectIds) throws Exception {
        logger.info("Inside deleteProjectWithTxn");

        ClientSession clientSession = mongoClient.startSession();

        try {
            clientSession.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());

            DeleteProjectResponseDTO deleteProjectResponseDTO = deleteProjectWithoutTxn(projectIds, clientSession);

            clientSession.commitTransaction();
            clientSession.close();

            return deleteProjectResponseDTO;
        } catch (Exception e) {
            clientSession.abortTransaction();
            clientSession.close();
            throw e;
        }
    }

    private DeleteProjectResponseDTO deleteProjectWithoutTxn(List<ObjectId> projectIds, ClientSession clientSession) throws Exception {
        logger.info("Inside deleteProjectWithoutTxn");

        projectDAO.deleteProjectsByIds(projectIds, clientSession);
        userProjectMappingDAO.deleteByProjectIds(projectIds, clientSession);
        fileDAO.deleteFilesForProject(projectIds, clientSession);
        datastoryDAO.deleteDatastoriesForProject(projectIds, clientSession);

        DeleteProjectResponseDTO deleteProjectResponseDTO = new DeleteProjectResponseDTO();
        deleteProjectResponseDTO.setStatus("SUCCESS");

        return deleteProjectResponseDTO;
    }
}
