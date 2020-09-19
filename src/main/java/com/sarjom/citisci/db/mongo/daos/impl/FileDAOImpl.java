package com.sarjom.citisci.db.mongo.daos.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.sarjom.citisci.db.mongo.daos.IFileDAO;
import com.sarjom.citisci.entities.File;
import com.sarjom.citisci.entities.User;
import com.sarjom.citisci.entities.UserOrganisationMapping;
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
public class FileDAOImpl implements IFileDAO {
    private static Logger logger = LoggerFactory.getLogger(FileDAOImpl.class);

    @Autowired
    @Qualifier("mongoCustom")
    MongoClient mongoClient;

    @Value("${mongo.database}")
    String databaseName;

    private MongoCollection<File> getCollection() {
        logger.info("Inside getCollection");

        return mongoClient.getDatabase(databaseName).getCollection("files", File.class);
    }

    @Override
    public void createFile(File file, ClientSession clientSession) throws Exception {
        logger.info("Inside createFile");

        if (file == null) {
            throw new Exception("file is null");
        }

        MongoCollection<File> files = getCollection();

        if (clientSession != null) {
            files.insertOne(clientSession, file);
        } else {
            files.insertOne(file);
        }
    }

    @Override
    public List<File> fetchByProjectId(ObjectId projectId) throws Exception {
        logger.info("Inside fetchByProjectId");

        if (projectId == null) {
            return new ArrayList<>();
        }

        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("project_id", projectId);

        return getCollection().find(basicDBObject).into(new ArrayList<>());
    }

    @Override
    public void deleteFilesForProject(List<ObjectId> projectIds, ClientSession clientSession) throws Exception {
        logger.info("Inside deleteFilesForProject");

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
