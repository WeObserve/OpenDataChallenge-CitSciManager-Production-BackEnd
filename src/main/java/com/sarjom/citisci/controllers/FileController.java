package com.sarjom.citisci.controllers;

import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.dtos.*;
import com.sarjom.citisci.services.IFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/files")
public class FileController {
    private static Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    IFileService fileService;

    @PostMapping(value = "")
    public ResponseDTO<CreateFileResponseDTO> createFile(HttpServletRequest httpServletRequest, @RequestBody CreateFileRequestDTO createFileRequestDTO) {
        logger.info("Inside createFile");

        ResponseDTO<CreateFileResponseDTO> responseDTO = new ResponseDTO<>();

        try {
            UserBO userBO = (UserBO) httpServletRequest.getAttribute("user");

            CreateFileResponseDTO createFileResponseDTO = fileService.createFile(createFileRequestDTO, userBO);

            responseDTO.setResponse(createFileResponseDTO);

            responseDTO.setStatus("SUCCESS");
            return responseDTO;
        } catch (Exception e) {
            responseDTO.setReason(e.getMessage());
            responseDTO.setStatus("FAILED");
            return responseDTO;
        }
    }

    @GetMapping(value = "/{projectId}")
    public ResponseDTO<DownloadFilesResponseDTO> downloadFiles(HttpServletRequest httpServletRequest, @PathVariable("projectId") String projectId) {
        logger.info("Inside createFile");

        ResponseDTO<DownloadFilesResponseDTO> responseDTO = new ResponseDTO<>();

        try {
            UserBO userBO = (UserBO) httpServletRequest.getAttribute("user");

            DownloadFilesResponseDTO downloadFilesResponseDTO = fileService.downloadFiles(projectId, userBO);

            fileService.sendFileLinks(downloadFilesResponseDTO.getProject(), userBO);

            downloadFilesResponseDTO.setProject(null);

            responseDTO.setResponse(downloadFilesResponseDTO);

            responseDTO.setStatus("SUCCESS");
            return responseDTO;
        } catch (Exception e) {
            responseDTO.setReason(e.getMessage());
            responseDTO.setStatus("FAILED");
            return responseDTO;
        }
    }
}
