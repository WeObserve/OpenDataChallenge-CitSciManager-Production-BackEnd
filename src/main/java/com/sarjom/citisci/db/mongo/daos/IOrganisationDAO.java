package com.sarjom.citisci.db.mongo.daos;

import com.sarjom.citisci.entities.Organisation;
import org.bson.types.ObjectId;

import java.util.List;

public interface IOrganisationDAO {
    List<Organisation> fetchByIds(List<ObjectId> ids) throws Exception;
}
