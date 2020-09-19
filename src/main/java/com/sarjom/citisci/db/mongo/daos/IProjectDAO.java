package com.sarjom.citisci.db.mongo.daos;

import com.mongodb.client.ClientSession;
import com.sarjom.citisci.entities.Organisation;
import com.sarjom.citisci.entities.Project;
import org.bson.types.ObjectId;

import java.util.List;

public interface IProjectDAO {
    void createProject(Project project, ClientSession clientSession) throws Exception;

    List<Project> getProjectsByName(String name) throws Exception;

    List<Project> fetchByIds(List<ObjectId> ids) throws Exception;

    void deleteProjectsByIds(List<ObjectId> ids, ClientSession clientSession) throws Exception;
}
