package com.sarjom.citisci.db.mongo.daos;

import com.mongodb.client.ClientSession;
import com.sarjom.citisci.entities.User;
import org.bson.types.ObjectId;

import java.util.List;

public interface IUserDAO {
    void createUser(User user, ClientSession clientSession) throws Exception;
    List<User> getUsersByEmail(String email) throws Exception;
    List<User> getUsersByEmails(List<String> emails) throws Exception;
    List<User> getUsersByIds(List<ObjectId> ids) throws Exception;
}
