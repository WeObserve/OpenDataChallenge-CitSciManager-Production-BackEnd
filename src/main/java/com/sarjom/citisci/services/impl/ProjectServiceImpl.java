package com.sarjom.citisci.services.impl;

import com.sarjom.citisci.bos.OrganisationBO;
import com.sarjom.citisci.bos.ProjectBO;
import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.db.mongo.daos.IOrganisationDAO;
import com.sarjom.citisci.db.mongo.daos.IProjectDAO;
import com.sarjom.citisci.db.mongo.daos.IUserOrganisationMappingDAO;
import com.sarjom.citisci.db.mongo.daos.IUserProjectMappingDAO;
import com.sarjom.citisci.dtos.*;
import com.sarjom.citisci.entities.Organisation;
import com.sarjom.citisci.entities.Project;
import com.sarjom.citisci.entities.UserOrganisationMapping;
import com.sarjom.citisci.entities.UserProjectMapping;
import com.sarjom.citisci.enums.ProjectType;
import com.sarjom.citisci.enums.Role;
import com.sarjom.citisci.services.IProjectService;
import com.sarjom.citisci.services.transactional.IProjectTransService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements IProjectService {
    private static Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Autowired
    IProjectDAO projectDAO;
    @Autowired
    IUserProjectMappingDAO userProjectMappingDAO;
    @Autowired
    IProjectTransService projectTransService;
    @Autowired
    IOrganisationDAO organisationDAO;
    @Autowired
    IUserOrganisationMappingDAO userOrganisationMappingDAO;

    @Override
    public CreateProjectResponseDTO createProject(CreateProjectRequestDTO createProjectRequestDTO, UserBO userBO) throws Exception {
        logger.info("Inside createProject");

        if (userBO == null || userBO.getRole() == null || !userBO.getRole().equals(Role.COLLECTOR)) {
            throw new Exception("User not authorized to create projects");
        }

        validateCreateProjectRequestDTO(createProjectRequestDTO, userBO);

        checkThatProjectWithThisNameDoesntExist(createProjectRequestDTO);

        ProjectBO projectBO = createProjectBO(createProjectRequestDTO);

        CreateProjectResponseDTO createProjectResponseDTO = projectTransService.createProject(projectBO, true);

        return createProjectResponseDTO;
    }

    private void checkThatProjectWithThisNameDoesntExist(CreateProjectRequestDTO createProjectRequestDTO) throws Exception {
        logger.info("Inside checkThatProjectWithThisNameDoesntExist");

        List<Project> projectsWithGivenName = projectDAO.getProjectsByName(createProjectRequestDTO.getName());

        if (!CollectionUtils.isEmpty(projectsWithGivenName)) {
            throw new Exception("There is already a project with this name");
        }
    }

    private ProjectBO createProjectBO(CreateProjectRequestDTO createProjectRequestDTO) throws Exception {
        logger.info("Inside createProjectBO");

        ProjectBO projectBO = new ProjectBO();

        BeanUtils.copyProperties(createProjectRequestDTO, projectBO);
        projectBO.setId(new ObjectId().toHexString());

        return projectBO;
    }

    private void validateCreateProjectRequestDTO(CreateProjectRequestDTO createProjectRequestDTO, UserBO userBO) throws Exception {
        logger.info("Inside validateCreateProjectRequestDTO");

        if (createProjectRequestDTO == null ||
                StringUtils.isEmpty(createProjectRequestDTO.getOrganisationId()) ||
                StringUtils.isEmpty(createProjectRequestDTO.getName()) ||
                StringUtils.isEmpty(createProjectRequestDTO.getCreatedByUserId()) ||
                StringUtils.isEmpty(createProjectRequestDTO.getLicense()) ||
                createProjectRequestDTO.getProjectType() == null) {
            throw new Exception("Invalid create project request");
        }

        if (!createProjectRequestDTO.getCreatedByUserId().equalsIgnoreCase(userBO.getId())) {
            throw new Exception("Id of user creating the project is not the one making the request");
        }

        checkThatOrganisationWithGivenIdExists(new ObjectId(createProjectRequestDTO.getOrganisationId()));
        checkThatUserIdIsLinkedToOrganisationId(new ObjectId(createProjectRequestDTO.getOrganisationId()),
                new ObjectId(userBO.getId()));
    }

    private void checkThatOrganisationWithGivenIdExists(ObjectId orgId) throws Exception {
        logger.info("Inside checkThatOrganisationwithGivenIdExists");

        List<Organisation> organisations = organisationDAO.fetchByIds(Arrays.asList(orgId));

        if (CollectionUtils.isEmpty(organisations)) {
            throw new Exception("No organisation with given id");
        }
    }

    private void checkThatUserIdIsLinkedToOrganisationId(ObjectId orgId, ObjectId userId) throws Exception {
        logger.info("Inside checkThatUserIdIsLinkedToOrganisationId");

        List<UserOrganisationMapping> userOrganisationMappings = userOrganisationMappingDAO.
                fetchByOrgIdAndUserId(orgId, userId);

        if (CollectionUtils.isEmpty(userOrganisationMappings)) {
            throw new Exception("Organisation and user are not linked");
        }
    }

    @Override
    public FetchAllProjectsForUserResponseDTO fetchAllProjectsForUser(UserBO userBO) throws Exception {
        logger.info("Inside fetchAllProjectsForUser");

        if (userBO == null || StringUtils.isEmpty(userBO.getId())) {
            throw new Exception("Please login again");
        }

        FetchAllProjectsForUserResponseDTO fetchAllProjectsForUserResponseDTO = new FetchAllProjectsForUserResponseDTO();
        List<ProjectBO> projectBOs = new ArrayList<>();

        fetchAllProjectsForUserResponseDTO.setProjects(projectBOs);

        List<UserProjectMapping> userProjectMappings = userProjectMappingDAO.fetchByUserId(new ObjectId(userBO.getId()));

        if (CollectionUtils.isEmpty(userProjectMappings)) {
            return fetchAllProjectsForUserResponseDTO;
        }

        List<ObjectId> projectIds = userProjectMappings.stream().map(UserProjectMapping::getProjectId).collect(Collectors.toList());

        List<Project> projects = projectDAO.fetchByIds(projectIds);

        if (CollectionUtils.isEmpty(projects)) {
            return fetchAllProjectsForUserResponseDTO;
        }

        populateProjectBOs(projects, projectBOs);

        return fetchAllProjectsForUserResponseDTO;
    }

    private void populateProjectBOs(List<Project> projects, List<ProjectBO> projectBOs) throws Exception {
        logger.info("Inside populateProjectBOs");

        List<ObjectId> organisationIds = new ArrayList<>();

        for (Project project: projects) {
            if (project == null || StringUtils.isEmpty(project.getName()) ||
                    project.getId() == null || project.getOrganisationId() == null ||
                    project.getCreatedByUserId() == null || StringUtils.isEmpty(project.getProjectType())) {
                continue;
            }

            organisationIds.add(project.getOrganisationId());
        }

        List<Organisation> organisations = organisationDAO.fetchByIds(organisationIds);

        Map<String, OrganisationBO> organisationIdToOrganisationBOMap = new HashMap<>();

        for (Organisation organisation: organisations) {
            organisationIdToOrganisationBOMap.put(organisation.getId().toHexString(),
                    convertToOrganisationBO(organisation));
        }


        for (Project project: projects) {
            if (project == null || StringUtils.isEmpty(project.getName()) ||
                project.getId() == null || project.getOrganisationId() == null ||
                project.getCreatedByUserId() == null || StringUtils.isEmpty(project.getProjectType())) {
                continue;
            }

            projectBOs.add(convertToProjectBO(project, organisationIdToOrganisationBOMap));
        }
    }

    private ProjectBO convertToProjectBO(Project project, Map<String, OrganisationBO> organisationIdToOrganisationBOMap) {
        logger.info("Inside convertToProjectBO");

        ProjectBO projectBO = new ProjectBO();

        BeanUtils.copyProperties(project, projectBO);

        projectBO.setId(project.getId().toHexString());
        projectBO.setOrganisationId(project.getOrganisationId().toHexString());
        projectBO.setCreatedByUserId(project.getCreatedByUserId().toHexString());
        projectBO.setProjectType(ProjectType.valueOf(project.getProjectType()));
        projectBO.setOrganisation(organisationIdToOrganisationBOMap.get(
                projectBO.getOrganisationId()
        ));

        return projectBO;
    }

    private OrganisationBO convertToOrganisationBO(Organisation organisation) {
        logger.info("Inside convertToOrganisationBO");

        OrganisationBO organisationBO = new OrganisationBO();

        organisationBO.setId(organisation.getId().toHexString());
        organisationBO.setEmail(organisation.getEmail());
        organisationBO.setName(organisation.getName());

        return organisationBO;
    }

    @Override
    public DeleteProjectResponseDTO deleteProject(DeleteProjectRequestDTO deleteProjectRequestDTO) throws Exception {
        logger.info("Inside deleteProject");

        List<ObjectId> projectIds = validateDeleteProjectRequestDTO(deleteProjectRequestDTO);

        return projectTransService.deleteProject(projectIds, true);
    }

    private List<ObjectId> validateDeleteProjectRequestDTO(DeleteProjectRequestDTO deleteProjectRequestDTO) throws Exception {
        logger.info("Inside validateDeleteProjectRequestDTO");

        if (deleteProjectRequestDTO == null ||
                CollectionUtils.isEmpty(deleteProjectRequestDTO.getProjectIds())) {
            throw new Exception("Invalid request");
        }

        List<ObjectId> projectIds = new ArrayList<>();

        for (String projectIdString: deleteProjectRequestDTO.getProjectIds()) {
            if (StringUtils.isEmpty(projectIdString)) {
                continue;
            }

            projectIds.add(new ObjectId(projectIdString));
        }

        return projectIds;
    }
}
