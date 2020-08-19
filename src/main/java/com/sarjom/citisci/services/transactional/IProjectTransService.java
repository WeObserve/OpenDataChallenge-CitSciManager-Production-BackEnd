package com.sarjom.citisci.services.transactional;

import com.sarjom.citisci.bos.ProjectBO;
import com.sarjom.citisci.dtos.CreateProjectResponseDTO;

public interface IProjectTransService {
    CreateProjectResponseDTO createProject(ProjectBO projectBO, Boolean useTxn) throws Exception;
}
