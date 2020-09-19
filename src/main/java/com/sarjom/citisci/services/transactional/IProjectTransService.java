package com.sarjom.citisci.services.transactional;

import com.sarjom.citisci.bos.ProjectBO;
import com.sarjom.citisci.dtos.CreateProjectResponseDTO;
import com.sarjom.citisci.dtos.DeleteProjectResponseDTO;
import org.bson.types.ObjectId;

import java.util.List;

public interface IProjectTransService {
    CreateProjectResponseDTO createProject(ProjectBO projectBO, Boolean useTxn) throws Exception;

    DeleteProjectResponseDTO deleteProject(List<ObjectId> projectIds, Boolean useTxn) throws Exception;
}
