package com.sarjom.citisci.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

@Getter
@Setter
@ToString
public class User {
    @BsonProperty(value = "_id")
    public ObjectId id;

    public String email;
    public String password;
    public String name;

    @BsonProperty(value = "org_name")
    public String orgName;

    @BsonProperty(value = "org_affiliation")
    public String orgAffiliation;
}
