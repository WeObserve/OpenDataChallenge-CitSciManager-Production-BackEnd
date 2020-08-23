package com.sarjom.citisci.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

@Getter
@Setter
@ToString
public class Feedback {
    @BsonProperty(value = "_id")
    public ObjectId id;

    @BsonProperty(value = "user_id")
    public ObjectId userId;

    public String comments;
}
