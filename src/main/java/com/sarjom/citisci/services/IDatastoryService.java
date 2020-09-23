package com.sarjom.citisci.services;

import com.sarjom.citisci.bos.DatastoryBO;
import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.dtos.*;

public interface IDatastoryService {
    CreateDatastoryResponseDTO createDatastory(CreateDatastoryRequestDTO createDatastoryRequestDTO, UserBO userBO) throws Exception;

    ViewDatastoryResponseDTO viewDatastory(String datastoryId, UserBO userBO) throws Exception;

    void sendDatastoryPublishedEmails(DatastoryBO createdDatastory);

    FetchDatastoryResponseDTO fetchDatastoriesForProject(String projectId, UserBO userBO) throws Exception;

    UpdateDraftDatastoryResponseDTO updateDraftDatastory(String datastoryId, UpdateDraftDatastoryRequestDTO updateDraftDatastoryRequestDTO, UserBO userBO) throws Exception;
}
