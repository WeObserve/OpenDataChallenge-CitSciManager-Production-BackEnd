package com.sarjom.citisci.db.mongo.daos.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.sarjom.citisci.db.mongo.daos.ISignUpInterestDAO;
import com.sarjom.citisci.entities.SignUpInterest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class SignUpInterestDAOImpl implements ISignUpInterestDAO {
    private static Logger logger = LoggerFactory.getLogger(SignUpInterestDAOImpl.class);

    @Autowired
    @Qualifier("mongoCustom")
    MongoClient mongoClient;

    @Value("${mongo.database}")
    String databaseName;

    private MongoCollection<SignUpInterest> getCollection() {
        logger.info("Inside getCollection");

        return mongoClient.getDatabase(databaseName).getCollection("sign_up_interests", SignUpInterest.class);
    }

    @Override
    public void createSignUpInterest(SignUpInterest signUpInterest, ClientSession clientSession) throws Exception {
        logger.info("Inside createSignUpInterest");

        if (signUpInterest == null) {
            throw new Exception("signUpInterest is null");
        }

        MongoCollection<SignUpInterest> signUpInterests = getCollection();

        if (clientSession != null) {
            signUpInterests.insertOne(clientSession, signUpInterest);
        } else {
            signUpInterests.insertOne(signUpInterest);
        }
    }
}
