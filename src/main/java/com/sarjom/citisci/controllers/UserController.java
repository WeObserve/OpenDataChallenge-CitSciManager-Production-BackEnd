package com.sarjom.citisci.controllers;

import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.dtos.*;
import com.sarjom.citisci.services.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/users")
public class UserController {
    private static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    IUserService userService;

    @PostMapping("/invite")
    public ResponseDTO<InviteUserResponseDTO> inviteUsers(HttpServletRequest httpServletRequest,
                                                         @RequestBody InviteUserRequestDTO inviteUserRequestDTO,
                                                         HttpServletResponse httpServletResponse) {
        logger.info("Inside inviteUsers");

        ResponseDTO<InviteUserResponseDTO> responseDTO = new ResponseDTO<>();

        try {
            UserBO userBO = (UserBO) httpServletRequest.getAttribute("user");

            InviteUserResponseDTO inviteUserResponseDTO = userService.inviteUser(inviteUserRequestDTO, userBO);

            userService.processInviteUsers(inviteUserRequestDTO, userBO, inviteUserResponseDTO.getProjectBO());
            
            responseDTO.setResponse(inviteUserResponseDTO);

            responseDTO.setStatus("SUCCESS");
            return responseDTO;
        } catch (Exception e) {
            responseDTO.setReason(e.getMessage());
            responseDTO.setStatus("FAILED");
            return responseDTO;
        }
    }
}
