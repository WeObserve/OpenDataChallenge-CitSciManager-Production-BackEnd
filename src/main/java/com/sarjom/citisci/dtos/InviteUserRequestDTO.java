package com.sarjom.citisci.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class InviteUserRequestDTO {
    public String organisationId;
    public String projectId;
    public String bucketName;
    public String userInvitationFileS3Key;
}
