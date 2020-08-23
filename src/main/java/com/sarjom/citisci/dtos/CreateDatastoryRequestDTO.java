package com.sarjom.citisci.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateDatastoryRequestDTO {
    public String projectId;
    public String name;
    public String type;
    public String content;
}
