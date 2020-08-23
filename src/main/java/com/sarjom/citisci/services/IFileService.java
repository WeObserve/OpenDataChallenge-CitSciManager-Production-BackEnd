package com.sarjom.citisci.services;

import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.dtos.CreateFileRequestDTO;
import com.sarjom.citisci.dtos.CreateFileResponseDTO;
import com.sarjom.citisci.dtos.DownloadFilesResponseDTO;
import com.sarjom.citisci.entities.Project;

public interface IFileService {
    CreateFileResponseDTO createFile(CreateFileRequestDTO createFileRequestDTO, UserBO userBO) throws Exception;
    void sendFileLinks(Project project, UserBO userBO) throws Exception;
    DownloadFilesResponseDTO downloadFiles(String projectId, UserBO userBO) throws Exception;
}

