package com.sarjom.citisci.services;

import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.dtos.CreateProjectRequestDTO;
import com.sarjom.citisci.dtos.CreateProjectResponseDTO;
import com.sarjom.citisci.dtos.FetchAllProjectsForUserResponseDTO;

public interface IProjectService {
    CreateProjectResponseDTO createProject(CreateProjectRequestDTO createProjectRequestDTO, UserBO userBO) throws Exception;
    FetchAllProjectsForUserResponseDTO fetchAllProjectsForUser(UserBO userBO) throws Exception;
}
