package com.sarjom.citisci.bos;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class DatastoryBO {
    public String id;
    public String projectId;
    public String createdByUserId;
    public String name;
    public String type;
    public String content;
    public List<FileBO> files;
    public UserBO createdByUser;
    public ProjectBO project;
}
