package com.sarjom.citisci.controllers;

import com.sarjom.citisci.dtos.LoginRequestDTO;
import com.sarjom.citisci.dtos.LoginResponseDTO;
import com.sarjom.citisci.dtos.ResponseDTO;
import com.sarjom.citisci.services.ILoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/login")
public class LoginController {
    private static Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    ILoginService loginService;

    @PostMapping("")
    public ResponseDTO<LoginResponseDTO> login(HttpServletRequest httpServletRequest, @RequestBody LoginRequestDTO loginRequestDTO) {
        logger.info("Inside login");

        ResponseDTO<LoginResponseDTO> response = new ResponseDTO<>();

        try {
            LoginResponseDTO loginResponseDTO = loginService.login(loginRequestDTO);

            response.setResponse(loginResponseDTO);
            response.setStatus("SUCCESS");
        } catch (Exception e) {
            response.setStatus("FAILED");
            response.setReason(e.getMessage());
        }

        return response;
    }
}
