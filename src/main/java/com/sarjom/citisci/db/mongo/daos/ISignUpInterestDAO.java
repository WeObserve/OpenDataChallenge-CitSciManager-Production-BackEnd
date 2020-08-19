package com.sarjom.citisci.db.mongo.daos;

import com.mongodb.client.ClientSession;
import com.sarjom.citisci.entities.SignUpInterest;

public interface ISignUpInterestDAO {
    void createSignUpInterest(SignUpInterest SignUpInterest, ClientSession clientSession) throws Exception;
}
