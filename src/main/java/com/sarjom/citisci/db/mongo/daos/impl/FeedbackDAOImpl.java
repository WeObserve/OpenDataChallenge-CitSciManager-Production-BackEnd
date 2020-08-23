package com.sarjom.citisci.db.mongo.daos.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.sarjom.citisci.db.mongo.daos.IFeedbackDAO;
import com.sarjom.citisci.db.mongo.daos.ISignUpInterestDAO;
import com.sarjom.citisci.entities.Feedback;
import com.sarjom.citisci.entities.SignUpInterest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class FeedbackDAOImpl implements IFeedbackDAO {
    private static Logger logger = LoggerFactory.getLogger(FeedbackDAOImpl.class);

    @Autowired
    @Qualifier("mongoCustom")
    MongoClient mongoClient;

    @Value("${mongo.database}")
    String databaseName;

    private MongoCollection<Feedback> getCollection() {
        logger.info("Inside getCollection");

        return mongoClient.getDatabase(databaseName).getCollection("feedbacks", Feedback.class);
    }

    @Override
    public void createFeedback(Feedback feedback, ClientSession clientSession) throws Exception {
        logger.info("Inside createFeedback");

        if (feedback == null) {
            throw new Exception("feedback is null");
        }

        MongoCollection<Feedback> feedbacks = getCollection();

        if (clientSession != null) {
            feedbacks.insertOne(clientSession, feedback);
        } else {
            feedbacks.insertOne(feedback);
        }
    }
}
