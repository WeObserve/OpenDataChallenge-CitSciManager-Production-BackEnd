package com.sarjom.citisci.services;

import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.dtos.*;

public interface IProjectService {
    CreateProjectResponseDTO createProject(CreateProjectRequestDTO createProjectRequestDTO, UserBO userBO) throws Exception;
    FetchAllProjectsForUserResponseDTO fetchAllProjectsForUser(UserBO userBO) throws Exception;
    DeleteProjectResponseDTO deleteProject(DeleteProjectRequestDTO deleteProjectRequestDTO) throws Exception;
}
