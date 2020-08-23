package com.sarjom.citisci.controllers;

import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.dtos.CreateDatastoryRequestDTO;
import com.sarjom.citisci.dtos.CreateDatastoryResponseDTO;
import com.sarjom.citisci.dtos.ResponseDTO;
import com.sarjom.citisci.dtos.ViewDatastoryResponseDTO;
import com.sarjom.citisci.services.IDatastoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/datastories")
public class DatastoryController {
    private static Logger logger = LoggerFactory.getLogger(DatastoryController.class);

    @Autowired
    IDatastoryService datastoryService;

    @PostMapping(value = "")
    public ResponseDTO<CreateDatastoryResponseDTO> createDatastory(HttpServletRequest httpServletRequest, @RequestBody CreateDatastoryRequestDTO createDatastoryRequestDTO) {
        logger.info("Inside createDatastory");

        ResponseDTO<CreateDatastoryResponseDTO> responseDTO = new ResponseDTO<>();

        try {
            UserBO userBO = (UserBO) httpServletRequest.getAttribute("user");

            CreateDatastoryResponseDTO createDatastoryResponseDTO = datastoryService.createDatastory(createDatastoryRequestDTO, userBO);

            datastoryService.sendDatastoryPublishedEmails(createDatastoryResponseDTO.getCreatedDatastory());

            responseDTO.setResponse(createDatastoryResponseDTO);

            responseDTO.setStatus("SUCCESS");
            return responseDTO;
        } catch (Exception e) {
            responseDTO.setReason(e.getMessage());
            responseDTO.setStatus("FAILED");
            return responseDTO;
        }
    }

    @GetMapping(value = "/{datastoryId}")
    public ResponseDTO<ViewDatastoryResponseDTO> viewDatastory(HttpServletRequest httpServletRequest,
                                                               @PathVariable("datastoryId") String datastoryId) {
        logger.info("Inside viewDatastory");

        ResponseDTO<ViewDatastoryResponseDTO> responseDTO = new ResponseDTO<>();

        try {
            UserBO userBO = (UserBO) httpServletRequest.getAttribute("user");

            ViewDatastoryResponseDTO viewDatastoryResponseDTO = datastoryService.viewDatastory(datastoryId, userBO);

            responseDTO.setResponse(viewDatastoryResponseDTO);

            responseDTO.setStatus("SUCCESS");
            return responseDTO;
        } catch (Exception e) {
            responseDTO.setReason(e.getMessage());
            responseDTO.setStatus("FAILED");
            return responseDTO;
        }
    }
}
