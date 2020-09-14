package com.sarjom.citisci.dtos;

import com.sarjom.citisci.bos.FileBO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class FetchFilesResponseDTO {
    public List<FileBO> files;
}