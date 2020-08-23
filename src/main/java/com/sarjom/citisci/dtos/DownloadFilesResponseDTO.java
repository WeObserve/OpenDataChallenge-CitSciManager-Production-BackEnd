package com.sarjom.citisci.dtos;

import com.sarjom.citisci.entities.Project;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DownloadFilesResponseDTO {
    public String status;
    public Project project;
}
