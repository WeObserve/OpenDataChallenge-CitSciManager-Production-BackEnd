package com.sarjom.citisci.dtos;

import com.sarjom.citisci.bos.FeedbackBO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateFeedbackResponseDTO {
    public FeedbackBO createdFeedback;
}
