package com.sarjom.citisci.db.mongo.daos.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.sarjom.citisci.db.mongo.daos.IDatastoryDAO;
import com.sarjom.citisci.entities.Datastory;
import com.sarjom.citisci.entities.User;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class DatastoryDAOImpl implements IDatastoryDAO {
    private static Logger logger = LoggerFactory.getLogger(DatastoryDAOImpl.class);

    @Autowired
    @Qualifier("mongoCustom")
    MongoClient mongoClient;

    @Value("${mongo.database}")
    String databaseName;

    private MongoCollection<Datastory> getCollection() {
        logger.info("Inside getCollection");

        return mongoClient.getDatabase(databaseName).getCollection("datastories", Datastory.class);
    }

    @Override
    public void createDatastory(Datastory datastory, ClientSession clientSession) throws Exception {
        logger.info("Inside createDatastory");

        if (datastory == null) {
            throw new Exception("datastory is null");
        }

        MongoCollection<Datastory> datastories = getCollection();

        if (clientSession != null) {
            datastories.insertOne(clientSession, datastory);
        } else {
            datastories.insertOne(datastory);
        }
    }

    @Override
    public void convertDraftToPublishedDatastory(ObjectId id, ClientSession clientSession) throws Exception {
        logger.info("Inside convertDraftToPublishedDatastory");

        if (id == null) {
            throw new Exception("datastory id is null");
        }

        MongoCollection<Datastory> datastories = getCollection();

        BasicDBObject query = new BasicDBObject();
        query.put("_id", id);

        BasicDBObject updatedDocument = new BasicDBObject();
        updatedDocument.put("is_draft", false);

        BasicDBObject update = new BasicDBObject();
        update.put("$set", updatedDocument);

        if (clientSession != null) {
            datastories.updateOne(clientSession, query, update);
        } else {
            datastories.updateOne(query, update);
        }
    }

    @Override
    public List<Datastory> getByIds(List<ObjectId> ids) throws Exception {
        logger.info("Inside getByIds");

        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }

        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("_id", new BasicDBObject("$in", ids));

        return getCollection().find(basicDBObject).into(new ArrayList<>());
    }

    @Override
    public List<Datastory> getByProjectIds(List<ObjectId> projectIds) throws Exception {
        logger.info("Inside getByProjectIds");

        if (CollectionUtils.isEmpty(projectIds)) {
            return new ArrayList<>();
        }

        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("project_id", new BasicDBObject("$in", projectIds));

        return getCollection().find(basicDBObject).into(new ArrayList<>());
    }
}
