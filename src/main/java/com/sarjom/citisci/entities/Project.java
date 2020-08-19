package com.sarjom.citisci.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.List;

@Getter
@Setter
@ToString
public class Project {
    @BsonProperty(value = "_id")
    public ObjectId id;

    @BsonProperty(value = "organisation_id")
    public ObjectId organisationId;

    @BsonProperty(value = "created_by_user_id")
    public ObjectId createdByUserId;

    public String name;
    public String description;

    @BsonProperty(value = "document_links")
    public List<String> documentLinks;

    @BsonProperty(value = "banner_link")
    public String bannerLink;

    @BsonProperty(value = "data_types")
    public List<String> dataTypes;

    @BsonProperty(value = "meta_data")
    public List<String> metaData;

    public String license;

    @BsonProperty(value = "project_type")
    public String projectType;
}
