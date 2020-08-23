package com.sarjom.citisci.bos;

import com.sarjom.citisci.entities.Datastory;
import com.sarjom.citisci.entities.Project;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ViewDatastoryRequestValidationResponseBO {
    public Datastory datastory;
    public Project project;
}
