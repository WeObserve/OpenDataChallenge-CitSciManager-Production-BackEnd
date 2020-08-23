package com.sarjom.citisci.db.mongo.daos;

import com.mongodb.client.ClientSession;
import com.sarjom.citisci.entities.Feedback;
import com.sarjom.citisci.entities.SignUpInterest;

public interface IFeedbackDAO {
    void createFeedback(Feedback feedback, ClientSession clientSession) throws Exception;
}
