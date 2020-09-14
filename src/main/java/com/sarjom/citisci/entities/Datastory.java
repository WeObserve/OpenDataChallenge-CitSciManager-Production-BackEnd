package com.sarjom.citisci.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

@Getter
@Setter
@ToString
public class Datastory {
    @BsonProperty(value = "_id")
    public ObjectId id;

    @BsonProperty(value = "project_id")
    public ObjectId projectId;

    @BsonProperty(value = "created_by_user_id")
    public ObjectId createdByUserId;

    public String name;
    public String type;
    public String content;

    @BsonProperty(value = "is_draft")
    public Boolean isDraft;
}
