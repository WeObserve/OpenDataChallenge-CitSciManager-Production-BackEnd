package com.sarjom.citisci.dtos;

import com.sarjom.citisci.enums.FileType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Getter
@Setter
@ToString
public class CreateFileRequestDTO {
    public String projectId;
    public String name;
    public String fileLink;
    public String comments;
    public String license;
    public BigDecimal latitude;
    public BigDecimal longitude;
    public Date createdAt;
    public String customTags;
    public String fileType;
}
