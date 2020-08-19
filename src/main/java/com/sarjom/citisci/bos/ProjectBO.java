package com.sarjom.citisci.bos;

import com.sarjom.citisci.enums.ProjectType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ProjectBO {
    public String id;
    public String organisationId;
    public String createdByUserId;
    public String name;
    public String description;
    public List<String> documentLinks;
    public String bannerLink;
    public List<String> dataTypes;
    public List<String> metaData;
    public ProjectType projectType;
}
