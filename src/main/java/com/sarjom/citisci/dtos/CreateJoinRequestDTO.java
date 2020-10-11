package com.sarjom.citisci.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class CreateJoinRequestDTO {
    public String projectId;
    public String fileId1;
    public String fileId2;
    public String joinColumnForFile1;
    public String joinColumnForFile2;
    public List<String> columnsForFile1;
    public List<String> columnsForFile2;
}
