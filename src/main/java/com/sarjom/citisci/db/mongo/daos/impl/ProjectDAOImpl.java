package com.sarjom.citisci.db.mongo.daos.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.sarjom.citisci.db.mongo.daos.IProjectDAO;
import com.sarjom.citisci.entities.Organisation;
import com.sarjom.citisci.entities.Project;
import com.sarjom.citisci.entities.User;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ProjectDAOImpl implements IProjectDAO {
    private static Logger logger = LoggerFactory.getLogger(ProjectDAOImpl.class);

    @Autowired
    @Qualifier("mongoCustom")
    MongoClient mongoClient;

    @Value("${mongo.database}")
    String databaseName;

    private MongoCollection<Project> getCollection() {
        logger.info("Inside getCollection");

        return mongoClient.getDatabase(databaseName).getCollection("projects", Project.class);
    }

    @Override
    public void createProject(Project project, ClientSession clientSession) throws Exception {
        logger.info("Inside createProject");

        if (project == null) {
            throw new Exception("project is null");
        }

        MongoCollection<Project> projects = getCollection();

        if (clientSession != null) {
            projects.insertOne(clientSession, project);
        } else {
            projects.insertOne(project);
        }
    }

    @Override
    public List<Project> getProjectsByName(String name) throws Exception {
        logger.info("Inside getProjectsByName");

        if (StringUtils.isEmpty(name)) {
            return new ArrayList<>();
        }

        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("name", name);

        return getCollection().find(basicDBObject).into(new ArrayList<>());
    }

    @Override
    public List<Project> fetchByIds(List<ObjectId> ids) throws Exception {
        logger.info("Inside fetchByIds");

        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }

        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("_id", new BasicDBObject("$in", ids));

        return getCollection().find(basicDBObject).into(new ArrayList<>());
    }

    @Override
    public void deleteProjectsByIds(List<ObjectId> projectIds, ClientSession clientSession) throws Exception {
        logger.info("Inside deleteProjectsById");

        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }

        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("_id", new BasicDBObject("$in", projectIds));

        if (clientSession == null) {
            getCollection().deleteMany(basicDBObject);
        } else {
            getCollection().deleteMany(clientSession, basicDBObject);
        }
    }
}
