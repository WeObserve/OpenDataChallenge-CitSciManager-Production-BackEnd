package com.sarjom.citisci.dtos;

import com.sarjom.citisci.bos.DatastoryBO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class FetchDatastoryResponseDTO {
    public List<DatastoryBO> datastories;
}
