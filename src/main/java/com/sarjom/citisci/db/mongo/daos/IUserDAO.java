package com.sarjom.citisci.db.mongo.daos;

import com.mongodb.client.ClientSession;
import com.sarjom.citisci.entities.User;

import java.util.List;

public interface IUserDAO {
    void createUser(User user, ClientSession clientSession) throws Exception;
    List<User> getUsersByEmail(String email) throws Exception;
}
