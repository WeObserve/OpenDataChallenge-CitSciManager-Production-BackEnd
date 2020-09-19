package com.sarjom.citisci.db.mongo.daos;

import com.mongodb.client.ClientSession;
import com.sarjom.citisci.entities.File;
import com.sarjom.citisci.entities.User;
import org.bson.types.ObjectId;

import java.util.List;

public interface IFileDAO {
    void createFile(File file, ClientSession clientSession) throws Exception;
    List<File> fetchByProjectId(ObjectId projectId) throws Exception;
    void deleteFilesForProject(List<ObjectId> projectIds, ClientSession clientSession) throws Exception;
}
