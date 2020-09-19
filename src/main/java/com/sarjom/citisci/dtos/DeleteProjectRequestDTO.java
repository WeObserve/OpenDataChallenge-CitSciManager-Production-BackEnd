package com.sarjom.citisci.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class DeleteProjectRequestDTO {
    public List<String> projectIds;
}
