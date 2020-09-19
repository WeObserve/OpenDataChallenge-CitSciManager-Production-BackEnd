package com.sarjom.citisci.db.mongo.daos.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.sarjom.citisci.db.mongo.daos.IUserProjectMappingDAO;
import com.sarjom.citisci.entities.Project;
import com.sarjom.citisci.entities.UserOrganisationMapping;
import com.sarjom.citisci.entities.UserProjectMapping;
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
public class UserProjectMappingDAOImpl implements IUserProjectMappingDAO {
    private static Logger logger = LoggerFactory.getLogger(UserProjectMappingDAOImpl.class);

    @Autowired
    @Qualifier("mongoCustom")
    MongoClient mongoClient;

    @Value("${mongo.database}")
    String databaseName;

    private MongoCollection<UserProjectMapping> getCollection() {
        logger.info("Inside getCollection");

        return mongoClient.getDatabase(databaseName).getCollection("user_project_mappings", UserProjectMapping.class);
    }

    @Override
    public void createUserProjectMapping(UserProjectMapping userProjectMapping, ClientSession clientSession) throws Exception {
        logger.info("Inside createUserProjectMapping");

        if (userProjectMapping == null) {
            throw new Exception("userProjectMapping is null");
        }

        MongoCollection<UserProjectMapping> userProjectMappings = getCollection();

        if (clientSession != null) {
            userProjectMappings.insertOne(clientSession, userProjectMapping);
        } else {
            userProjectMappings.insertOne(userProjectMapping);
        }
    }

    @Override
    public List<UserProjectMapping> fetchByUserId(ObjectId userId) throws Exception {
        logger.info("Inside fetchByUserId");

        if (userId == null) {
            return new ArrayList<>();
        }

        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("user_id", userId);

        return getCollection().find(basicDBObject).into(new ArrayList<>());
    }

    @Override
    public List<UserProjectMapping> fetchByProjectIdAndUserIds(ObjectId projectId, List<ObjectId> userIds) throws Exception {
        logger.info("Inside fetchByUserId");

        if (projectId == null || CollectionUtils.isEmpty(userIds)) {
            return new ArrayList<>();
        }

        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("user_id", new BasicDBObject("$in", userIds));
        basicDBObject.put("project_id", projectId);

        return getCollection().find(basicDBObject).into(new ArrayList<>());
    }

    @Override
    public void deleteByProjectIds(List<ObjectId> projectIds, ClientSession clientSession) throws Exception {
        logger.info("Inside deleteByProjectIds");

        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }

        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("project_id", new BasicDBObject("$in", projectIds));

        if (clientSession == null) {
            getCollection().deleteMany(basicDBObject);
        } else {
            getCollection().deleteMany(clientSession, basicDBObject);
        }
    }
}
