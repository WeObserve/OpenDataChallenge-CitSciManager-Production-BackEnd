package com.sarjom.citisci.dtos;

import com.sarjom.citisci.bos.DatastoryBO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ViewDatastoryResponseDTO {
    public DatastoryBO datastory;
}
