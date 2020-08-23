package com.sarjom.citisci.services;

import com.sarjom.citisci.bos.DatastoryBO;
import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.dtos.CreateDatastoryRequestDTO;
import com.sarjom.citisci.dtos.CreateDatastoryResponseDTO;
import com.sarjom.citisci.dtos.ViewDatastoryResponseDTO;

public interface IDatastoryService {
    CreateDatastoryResponseDTO createDatastory(CreateDatastoryRequestDTO createDatastoryRequestDTO, UserBO userBO) throws Exception;

    ViewDatastoryResponseDTO viewDatastory(String datastoryId, UserBO userBO) throws Exception;

    void sendDatastoryPublishedEmails(DatastoryBO createdDatastory);
}
