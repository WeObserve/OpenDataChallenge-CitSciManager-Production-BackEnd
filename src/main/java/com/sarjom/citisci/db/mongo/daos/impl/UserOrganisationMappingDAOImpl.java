package com.sarjom.citisci.db.mongo.daos.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.sarjom.citisci.db.mongo.daos.IUserOrganisationMappingDAO;
import com.sarjom.citisci.entities.User;
import com.sarjom.citisci.entities.UserOrganisationMapping;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class UserOrganisationMappingDAOImpl implements IUserOrganisationMappingDAO {
    private static Logger logger = LoggerFactory.getLogger(UserOrganisationMappingDAOImpl.class);

    @Autowired
    @Qualifier("mongoCustom")
    MongoClient mongoClient;

    @Value("${mongo.database}")
    String databaseName;

    private MongoCollection<UserOrganisationMapping> getCollection() {
        logger.info("Inside getCollection");

        return mongoClient.getDatabase(databaseName).getCollection("user_organisation_mappings", UserOrganisationMapping.class);
    }

    @Override
    public List<UserOrganisationMapping> fetchByUserId(ObjectId userId) throws Exception {
        logger.info("Inside fetchByUserId");

        if (userId == null) {
            return new ArrayList<>();
        }

        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("user_id", userId);

        return getCollection().find(basicDBObject).into(new ArrayList<>());
    }
}
