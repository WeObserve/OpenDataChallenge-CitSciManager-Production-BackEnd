package com.sarjom.citisci.db.mongo.daos.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.ClientSession;
import com.sarjom.citisci.db.mongo.daos.IUserDAO;
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
public class UserDAOImpl implements IUserDAO {
    private static Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);

    @Autowired @Qualifier("mongoCustom")
    MongoClient mongoClient;

    @Value("${mongo.database}")
    String databaseName;

    private MongoCollection<User> getCollection() {
        logger.info("Inside getCollection");

        return mongoClient.getDatabase(databaseName).getCollection("users", User.class);
    }

    @Override
    public void createUser(User user, ClientSession clientSession) throws Exception {
        logger.info("Inside createUser");

        if (user == null) {
            throw new Exception("user is null");
        }

        MongoCollection<User> users = getCollection();

        if (clientSession != null) {
            users.insertOne(clientSession, user);
        } else {
            users.insertOne(user);
        }
    }

    @Override
    public List<User> getUsersByEmail(String email) throws Exception {
        logger.info("Inside getUsersByEmail");

        if (StringUtils.isEmpty(email)) {
            return new ArrayList<>();
        }

        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("email", email);

        return getCollection().find(basicDBObject).into(new ArrayList<>());
    }

    @Override
    public List<User> getUsersByEmails(List<String> emails) throws Exception {
        logger.info("Inside getUsersByEmails");

        if (CollectionUtils.isEmpty(emails)) {
            return new ArrayList<>();
        }

        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("email", new BasicDBObject("$in", emails));

        return getCollection().find(basicDBObject).into(new ArrayList<>());
    }

    @Override
    public List<User> getUsersByIds(List<ObjectId> ids) throws Exception {
        logger.info("Inside getUsersByIds");

        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }

        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("_id", new BasicDBObject("$in", ids));

        return getCollection().find(basicDBObject).into(new ArrayList<>());
    }
}
