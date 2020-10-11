package com.sarjom.citisci.dtos;

import com.sarjom.citisci.bos.JoinBO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class CreateJoinResponseDTO {
    JoinBO join;
}
