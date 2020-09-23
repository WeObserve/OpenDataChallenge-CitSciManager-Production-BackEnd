package com.sarjom.citisci.dtos;

import lombok.Data;

@Data
public class UpdateDraftDatastoryRequestDTO {
    public String name;
    public String type;
    public String content;
    public Boolean isDraft;
}
