package com.sarjom.citisci.db.mongo.daos;

import com.mongodb.client.ClientSession;
import com.sarjom.citisci.entities.Datastory;
import com.sarjom.citisci.entities.User;
import org.bson.types.ObjectId;

import java.util.List;

public interface IDatastoryDAO {
    void createDatastory(Datastory datastory, ClientSession clientSession) throws Exception;
    List<Datastory> getByIds(List<ObjectId> ids) throws Exception;
}
