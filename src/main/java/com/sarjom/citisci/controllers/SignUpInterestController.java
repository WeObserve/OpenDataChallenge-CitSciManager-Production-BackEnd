package com.sarjom.citisci.controllers;

import com.sarjom.citisci.dtos.*;
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
@RequestMapping("/sign-up-interest")
public class SignUpInterestController {
    private static Logger logger = LoggerFactory.getLogger(SignUpInterestController.class);

    @Autowired
    ISignUpInterestService signUpInterestService;

    @PostMapping("")
    public ResponseDTO<CreateSignUpInterestResponseDTO> createSignUpInterest(HttpServletRequest httpServletRequest,
                                                                   @RequestBody CreateSignUpInterestRequestDTO createSignUpInterestRequestDTO,
                                                                   HttpServletResponse httpServletResponse) {
        logger.info("Inside createSignUpInterest");

        ResponseDTO<CreateSignUpInterestResponseDTO> responseDTO = new ResponseDTO<>();

        try {
            CreateSignUpInterestResponseDTO createSignUpInterestResponseDTO = signUpInterestService.createSignUpInterest(createSignUpInterestRequestDTO);

            responseDTO.setResponse(createSignUpInterestResponseDTO);

            responseDTO.setStatus("SUCCESS");
            return responseDTO;
        } catch (Exception e) {
            responseDTO.setReason(e.getMessage());
            responseDTO.setStatus("FAILED");
            return responseDTO;
        }
    }
}
