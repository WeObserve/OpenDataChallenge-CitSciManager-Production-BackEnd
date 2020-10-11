package com.sarjom.citisci.controllers;

import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.dtos.CreateFileResponseDTO;
import com.sarjom.citisci.dtos.CreateJoinRequestDTO;
import com.sarjom.citisci.dtos.CreateJoinResponseDTO;
import com.sarjom.citisci.dtos.ResponseDTO;
import com.sarjom.citisci.services.IJoinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/joins")
@Slf4j
public class JoinController {
    @Autowired private IJoinService joinService;

    @PostMapping(value = "")
    public ResponseDTO<CreateJoinResponseDTO> createJoin(HttpServletRequest httpServletRequest,
                                                         @RequestBody CreateJoinRequestDTO createJoinRequestDTO) {
        log.info("Inside createJoin");

        ResponseDTO<CreateJoinResponseDTO> responseDTO = new ResponseDTO<>();

        try {
            UserBO userBO = (UserBO) httpServletRequest.getAttribute("user");

            CreateJoinResponseDTO createJoinResponseDTO = joinService.createJoin(
                    createJoinRequestDTO, userBO);

            responseDTO.setResponse(createJoinResponseDTO);

            responseDTO.setStatus("SUCCESS");
            return responseDTO;
        } catch (Exception e) {
            responseDTO.setReason(e.getMessage());
            responseDTO.setStatus("FAILED");
            return responseDTO;
        }
    }
}
