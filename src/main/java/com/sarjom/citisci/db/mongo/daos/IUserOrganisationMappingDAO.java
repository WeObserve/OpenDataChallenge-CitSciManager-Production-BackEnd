package com.sarjom.citisci.db.mongo.daos;

import com.sarjom.citisci.entities.UserOrganisationMapping;
import org.bson.types.ObjectId;

import java.util.List;

public interface IUserOrganisationMappingDAO {
    List<UserOrganisationMapping> fetchByUserId(ObjectId userId) throws Exception;
}
