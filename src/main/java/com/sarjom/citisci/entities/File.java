package com.sarjom.citisci.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@ToString
public class File {
    @BsonProperty(value = "_id")
    public ObjectId id;

    @BsonProperty(value = "uploaded_by_user_id")
    public ObjectId uploadedByUserId;

    @BsonProperty(value = "project_id")
    public ObjectId projectId;

    public String name;

    @BsonProperty(value = "file_link")
    public String fileLink;

    public BigDecimal latitude;
    public BigDecimal longitude;

    @BsonProperty(value = "created_at")
    public Date createdAt;

    @BsonProperty(value = "custom_tags")
    public String customTags;

    public String license;
    public String comments;
    public String fileType;
}
