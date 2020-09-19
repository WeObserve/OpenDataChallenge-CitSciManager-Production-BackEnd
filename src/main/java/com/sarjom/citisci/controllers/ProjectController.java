package com.sarjom.citisci.controllers;

import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.dtos.*;
import com.sarjom.citisci.services.IProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/projects")
public class ProjectController {
    private static Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    IProjectService projectService;

    @PostMapping("")
    public ResponseDTO<CreateProjectResponseDTO> createProject(HttpServletRequest httpServletRequest,
                                                            @RequestBody CreateProjectRequestDTO createProjectRequestDTO,
                                                            HttpServletResponse httpServletResponse) {
        logger.info("Inside createProject");

        ResponseDTO<CreateProjectResponseDTO> responseDTO = new ResponseDTO<>();

        try {
            UserBO userBO = (UserBO) httpServletRequest.getAttribute("user");

            CreateProjectResponseDTO createProjectResponseDTO = projectService.createProject(createProjectRequestDTO, userBO);

            responseDTO.setResponse(createProjectResponseDTO);

            responseDTO.setStatus("SUCCESS");
            return responseDTO;
        } catch (Exception e) {
            responseDTO.setReason(e.getMessage());
            responseDTO.setStatus("FAILED");
            return responseDTO;
        }
    }

    @DeleteMapping("")
    public ResponseDTO<DeleteProjectResponseDTO> deleteProject(HttpServletRequest httpServletRequest,
                                                               @RequestBody DeleteProjectRequestDTO deleteProjectRequestDTO,
                                                               HttpServletResponse httpServletResponse) {
        logger.info("Inside deleteProject");

        ResponseDTO<DeleteProjectResponseDTO> responseDTO = new ResponseDTO<>();

        try {
            UserBO userBO = (UserBO) httpServletRequest.getAttribute("user");

            DeleteProjectResponseDTO deleteProjectResponseDTO = projectService.deleteProject(deleteProjectRequestDTO);

            responseDTO.setResponse(deleteProjectResponseDTO);

            responseDTO.setStatus("SUCCESS");
            return responseDTO;
        } catch (Exception e) {
            responseDTO.setReason(e.getMessage());
            responseDTO.setStatus("FAILED");
            return responseDTO;
        }
    }

    @GetMapping("")
    public ResponseDTO<FetchAllProjectsForUserResponseDTO> fetchAllProjectsForUser(HttpServletRequest httpServletRequest,
                                                                                   HttpServletResponse httpServletResponse) {
        logger.info("Inside fetchAllProjectsForUser");

        ResponseDTO<FetchAllProjectsForUserResponseDTO> responseDTO = new ResponseDTO<>();

        try {
            UserBO userBO = (UserBO) httpServletRequest.getAttribute("user");

            FetchAllProjectsForUserResponseDTO fetchAllProjectsForUserResponseDTO = projectService.fetchAllProjectsForUser(userBO);

            responseDTO.setResponse(fetchAllProjectsForUserResponseDTO);

            responseDTO.setStatus("SUCCESS");
            return responseDTO;
        } catch (Exception e) {
            responseDTO.setReason(e.getMessage());
            responseDTO.setStatus("FAILED");
            return responseDTO;
        }
    }
}
