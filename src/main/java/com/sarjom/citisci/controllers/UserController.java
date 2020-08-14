package com.sarjom.citisci.controllers;

import com.sarjom.citisci.dtos.CreateUserRequestDTO;
import com.sarjom.citisci.dtos.CreateUserResponseDTO;
import com.sarjom.citisci.dtos.ResponseDTO;
import com.sarjom.citisci.services.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/user")
public class UserController {
    private static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    IUserService userService;

    @PostMapping("")
    public ResponseDTO<CreateUserResponseDTO> createUser(HttpServletRequest httpServletRequest,
                                                         @RequestBody CreateUserRequestDTO createUserRequestDTO,
                                                         HttpServletResponse httpServletResponse) {
        logger.info("Inside createUser");

        ResponseDTO<CreateUserResponseDTO> responseDTO = new ResponseDTO<>();

        try {
            CreateUserResponseDTO createUserResponseDTO = userService.createUser(createUserRequestDTO);

            responseDTO.setResponse(createUserResponseDTO);

            responseDTO.setStatus("SUCCESS");
            return responseDTO;
        } catch (Exception e) {
            responseDTO.setReason(e.getMessage());
            responseDTO.setStatus("FAILED");
            return responseDTO;
        }
    }
}
