package com.sarjom.citisci.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResponseDTO<T> {
    String reason;
    T response;
    String status;
}
