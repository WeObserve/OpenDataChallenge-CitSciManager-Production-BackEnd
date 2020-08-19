package com.sarjom.citisci.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

@Getter
@Setter
@ToString
public class SignUpInterest {
    @BsonProperty(value = "_id")
    public ObjectId id;

    public String description;

    @BsonProperty(value = "email_list")
    public String emailList;
}
