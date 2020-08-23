package com.sarjom.citisci.services;

import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.dtos.CreateFeedbackRequestDTO;
import com.sarjom.citisci.dtos.CreateFeedbackResponseDTO;
import com.sarjom.citisci.dtos.CreateSignUpInterestRequestDTO;
import com.sarjom.citisci.dtos.CreateSignUpInterestResponseDTO;

public interface IFeedbackService {
    CreateFeedbackResponseDTO createFeedback(CreateFeedbackRequestDTO createFeedbackRequestDTO,
                                             UserBO userBO) throws Exception;
}
