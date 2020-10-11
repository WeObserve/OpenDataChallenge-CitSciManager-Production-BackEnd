package com.sarjom.citisci.db.mongo.daos;

import com.mongodb.client.ClientSession;
import com.sarjom.citisci.entities.Join;

public interface IJoinDAO {
    void createJoin(Join join, ClientSession clientSession) throws Exception;
}
