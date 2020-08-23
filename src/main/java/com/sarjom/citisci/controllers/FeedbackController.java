package com.sarjom.citisci.controllers;

import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.dtos.CreateFeedbackRequestDTO;
import com.sarjom.citisci.dtos.CreateSignUpInterestRequestDTO;
import com.sarjom.citisci.dtos.CreateFeedbackResponseDTO;
import com.sarjom.citisci.dtos.ResponseDTO;
import com.sarjom.citisci.services.IFeedbackService;
import com.sarjom.citisci.services.ISignUpInterestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/feedbacks")
public class FeedbackController {
    private static Logger logger = LoggerFactory.getLogger(FeedbackController.class);

    @Autowired
    IFeedbackService feedbackService;

    @PostMapping("")
    public ResponseDTO<CreateFeedbackResponseDTO> createFeedback(HttpServletRequest httpServletRequest,
                                                                   @RequestBody CreateFeedbackRequestDTO createFeedbackRequestDTO,
                                                                   HttpServletResponse httpServletResponse) {
        logger.info("Inside createFeedback");

        ResponseDTO<CreateFeedbackResponseDTO> responseDTO = new ResponseDTO<>();

        try {
            UserBO userBO = (UserBO) httpServletRequest.getAttribute("user");

            CreateFeedbackResponseDTO createFeedbackResponseDTO = feedbackService.createFeedback(createFeedbackRequestDTO, userBO);

            responseDTO.setResponse(createFeedbackResponseDTO);

            responseDTO.setStatus("SUCCESS");
            return responseDTO;
        } catch (Exception e) {
            responseDTO.setReason(e.getMessage());
            responseDTO.setStatus("FAILED");
            return responseDTO;
        }
    }
}
