package com.sarjom.citisci.db.mongo.daos;

import com.mongodb.client.ClientSession;
import com.sarjom.citisci.entities.Datastory;
import com.sarjom.citisci.entities.User;
import org.bson.types.ObjectId;

import java.util.List;

public interface IDatastoryDAO {
    void createDatastory(Datastory datastory, ClientSession clientSession) throws Exception;
    List<Datastory> getByIds(List<ObjectId> ids) throws Exception;
    List<Datastory> getByProjectIds(List<ObjectId> projectIds) throws Exception;
    void convertDraftToPublishedDatastory(ObjectId id, ClientSession clientSession) throws Exception;
    void deleteDatastoriesForProject(List<ObjectId> projectIds, ClientSession clientSession) throws Exception;
}
