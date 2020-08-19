package com.sarjom.citisci.dtos;

import com.sarjom.citisci.bos.ProjectBO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class FetchAllProjectsForUserResponseDTO {
    List<ProjectBO> projects;
}
