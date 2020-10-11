package com.sarjom.citisci.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
public class Join {
    @BsonProperty(value = "_id")
    public ObjectId id;

    @BsonProperty(value = "user_id")
    public ObjectId userId;

    @BsonProperty(value = "project_id")
    public ObjectId projectId;

    @BsonProperty(value = "file_id_1")
    public ObjectId fileId1;

    @BsonProperty(value = "file_id_2")
    public ObjectId fileId2;

    @BsonProperty(value = "columns_for_file_1")
    public List<String> columnsForFile1;

    @BsonProperty(value = "columns_for_file_2")
    public List<String> columnsForFile2;

    @BsonProperty(value = "join_column_for_file_1")
    public String joinColumnForFile1;

    @BsonProperty(value = "join_column_for_file_2")
    public String joinColumnForFile2;

    public String status;
}
