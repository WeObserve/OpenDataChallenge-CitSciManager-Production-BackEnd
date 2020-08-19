package com.sarjom.citisci.db.mongo.daos.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.sarjom.citisci.db.mongo.daos.IOrganisationDAO;
import com.sarjom.citisci.entities.Organisation;
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
public class OrganisationDAOImpl implements IOrganisationDAO {
    private static Logger logger = LoggerFactory.getLogger(OrganisationDAOImpl.class);

    @Autowired
    @Qualifier("mongoCustom")
    MongoClient mongoClient;

    @Value("${mongo.database}")
    String databaseName;

    private MongoCollection<Organisation> getCollection() {
        logger.info("Inside getCollection");

        return mongoClient.getDatabase(databaseName).getCollection("organisations", Organisation.class);
    }

    @Override
    public List<Organisation> fetchByIds(List<ObjectId> ids) throws Exception {
        logger.info("Inside fetchById");

        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }

        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("_id", new BasicDBObject("$in", ids));

        return getCollection().find(basicDBObject).into(new ArrayList<>());
    }
}
