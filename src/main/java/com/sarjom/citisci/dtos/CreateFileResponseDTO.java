package com.sarjom.citisci.dtos;

import com.sarjom.citisci.bos.FileBO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateFileResponseDTO {
    public FileBO createdFile;
}
