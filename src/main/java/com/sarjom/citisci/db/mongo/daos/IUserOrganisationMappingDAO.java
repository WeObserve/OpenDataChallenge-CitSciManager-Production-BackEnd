package com.sarjom.citisci.db.mongo.daos;

import com.mongodb.client.ClientSession;
import com.sarjom.citisci.entities.UserOrganisationMapping;
import com.sarjom.citisci.entities.UserProjectMapping;
import org.bson.types.ObjectId;

import java.util.List;

public interface IUserOrganisationMappingDAO {
    List<UserOrganisationMapping> fetchByUserId(ObjectId userId) throws Exception;

    List<UserOrganisationMapping> fetchByOrgIdAndUserId(ObjectId orgId, ObjectId userId) throws Exception;

    List<UserOrganisationMapping> fetchByOrgIdAndUserIds(ObjectId orgId, List<ObjectId> userIds) throws Exception;

    void createUserOrganisationMapping(UserOrganisationMapping userOrganisationMapping, ClientSession clientSession) throws Exception;
}
