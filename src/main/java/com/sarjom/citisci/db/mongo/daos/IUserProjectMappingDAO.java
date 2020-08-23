package com.sarjom.citisci.db.mongo.daos;

import com.mongodb.client.ClientSession;
import com.sarjom.citisci.entities.UserProjectMapping;
import org.bson.types.ObjectId;

import java.util.List;

public interface IUserProjectMappingDAO {
    void createUserProjectMapping(UserProjectMapping userProjectMapping, ClientSession clientSession) throws Exception;
    List<UserProjectMapping> fetchByUserId(ObjectId userId) throws Exception;
    List<UserProjectMapping> fetchByProjectIdAndUserIds(ObjectId projectId, List<ObjectId> userIds) throws Exception;
}
