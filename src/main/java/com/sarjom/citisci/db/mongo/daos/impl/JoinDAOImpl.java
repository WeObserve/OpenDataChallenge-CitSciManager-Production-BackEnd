package com.sarjom.citisci.db.mongo.daos.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.sarjom.citisci.db.mongo.daos.IJoinDAO;
import com.sarjom.citisci.entities.File;
import com.sarjom.citisci.entities.Join;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class JoinDAOImpl implements IJoinDAO {
    @Autowired
    @Qualifier("mongoCustom")
    MongoClient mongoClient;

    @Value("${mongo.database}")
    String databaseName;

    private MongoCollection<Join> getCollection() {
        log.info("Inside getCollection");

        return mongoClient.getDatabase(databaseName).getCollection("joins", Join.class);
    }

    @Override
    public void createJoin(Join join, ClientSession clientSession) throws Exception {
        log.info("Inside createJoin");

        if (join == null) {
            throw new Exception("join is null");
        }

        MongoCollection<Join> joins = getCollection();

        if (clientSession != null) {
            joins.insertOne(clientSession, join);
        } else {
            joins.insertOne(join);
        }
    }
}