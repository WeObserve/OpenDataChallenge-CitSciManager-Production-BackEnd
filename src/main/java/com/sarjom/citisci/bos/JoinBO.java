package com.sarjom.citisci.bos;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.List;

@Getter
@Setter
@ToString
public class JoinBO {
    public String id;
    public String userId;
    public String projectId;
    public String fileId1;
    public String fileId2;
    public List<String> columnsForFile1;
    public List<String> columnsForFile2;
    public String joinColumnForFile1;
    public String joinColumnForFile2;
    public String status;
}
