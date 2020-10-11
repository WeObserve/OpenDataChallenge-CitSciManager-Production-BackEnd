package com.sarjom.citisci.services.impl;

import com.sarjom.citisci.bos.FileBO;
import com.sarjom.citisci.bos.JoinBO;
import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.db.mongo.daos.IFileDAO;
import com.sarjom.citisci.db.mongo.daos.IJoinDAO;
import com.sarjom.citisci.db.mongo.daos.IProjectDAO;
import com.sarjom.citisci.db.mongo.daos.IUserProjectMappingDAO;
import com.sarjom.citisci.dtos.CreateFileRequestDTO;
import com.sarjom.citisci.dtos.CreateFileResponseDTO;
import com.sarjom.citisci.dtos.CreateJoinRequestDTO;
import com.sarjom.citisci.dtos.CreateJoinResponseDTO;
import com.sarjom.citisci.entities.File;
import com.sarjom.citisci.entities.Join;
import com.sarjom.citisci.entities.Project;
import com.sarjom.citisci.entities.UserProjectMapping;
import com.sarjom.citisci.enums.FileStatus;
import com.sarjom.citisci.enums.FileType;
import com.sarjom.citisci.enums.JoinStatus;
import com.sarjom.citisci.services.IJoinService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JoinServiceImpl implements IJoinService {
    @Autowired private IProjectDAO projectDAO;
    @Autowired private IUserProjectMappingDAO userProjectMappingDAO;
    @Autowired private IFileDAO fileDAO;
    @Autowired private IJoinDAO joinDAO;

    @Override
    public CreateJoinResponseDTO createJoin(CreateJoinRequestDTO createJoinRequestDTO, UserBO userBO) throws Exception {
        log.info("Inside createJoin");

        validateCreateJoinRequestDTO(createJoinRequestDTO, userBO);

        Join  join = populateJoin(createJoinRequestDTO, userBO);

        joinDAO.createJoin(join, null);

        JoinBO joinBO = convertToJoinBO(join);

        CreateJoinResponseDTO createJoinResponseDTO = new CreateJoinResponseDTO();
        createJoinResponseDTO.setJoin(joinBO);

        return createJoinResponseDTO;
    }

    private JoinBO convertToJoinBO(Join join) {
        JoinBO joinBO = new JoinBO();

        BeanUtils.copyProperties(join, joinBO);

        joinBO.setId(join.getId().toHexString());
        joinBO.setProjectId(join.getProjectId().toHexString());
        joinBO.setUserId(join.getUserId().toHexString());
        joinBO.setFileId1(join.getFileId1().toHexString());
        joinBO.setFileId2(join.getFileId2().toHexString());

        return joinBO;
    }

    private Join populateJoin(CreateJoinRequestDTO createJoinRequestDTO, UserBO userBO) {
        log.info("Inside populateJoin");

        Join join = new Join();

        BeanUtils.copyProperties(createJoinRequestDTO, join);

        join.setId(new ObjectId());
        join.setProjectId(new ObjectId(createJoinRequestDTO.getProjectId()));
        join.setUserId(new ObjectId(userBO.getId()));
        join.setFileId1(new ObjectId(createJoinRequestDTO.getFileId1()));
        join.setFileId2(new ObjectId(createJoinRequestDTO.getFileId2()));
        join.setStatus(JoinStatus.PENDING.name());

        return join;
    }

    private void validateCreateJoinRequestDTO(CreateJoinRequestDTO createJoinRequestDTO, UserBO userBO) throws Exception {
        log.info("Inside validateCreateJoinRequestDTO");

        if (createJoinRequestDTO == null ||
                StringUtils.isEmpty(createJoinRequestDTO.getProjectId()) ||
                StringUtils.isEmpty(createJoinRequestDTO.getFileId1()) ||
                StringUtils.isEmpty(createJoinRequestDTO.getFileId2()) ||
                StringUtils.isEmpty(createJoinRequestDTO.getJoinColumnForFile1()) ||
                StringUtils.isEmpty(createJoinRequestDTO.getJoinColumnForFile2()) ||
                CollectionUtils.isEmpty(createJoinRequestDTO.getColumnsForFile1()) ||
                CollectionUtils.isEmpty(createJoinRequestDTO.getColumnsForFile2())) {
            throw new Exception("Invalid request");
        }

        createJoinRequestDTO.setColumnsForFile1(
                createJoinRequestDTO.getColumnsForFile1().stream()
                        .filter(column -> !StringUtils.isEmpty(column))
                        .distinct().collect(Collectors.toList()));
        createJoinRequestDTO.setColumnsForFile2(
                createJoinRequestDTO.getColumnsForFile2().stream()
                        .filter(column -> !StringUtils.isEmpty(column))
                        .distinct().collect(Collectors.toList()));

        checkIfJoinColumnIsInSelectedColumns(createJoinRequestDTO.getColumnsForFile1(), createJoinRequestDTO.getJoinColumnForFile1());
        checkIfJoinColumnIsInSelectedColumns(createJoinRequestDTO.getColumnsForFile2(), createJoinRequestDTO.getJoinColumnForFile2());

        List<Project> projects = projectDAO.fetchByIds(Arrays.asList(new ObjectId(createJoinRequestDTO.getProjectId())));

        if (CollectionUtils.isEmpty(projects)) {
            throw new Exception("Project does not exist");
        }

        if (userBO == null || StringUtils.isEmpty(userBO.getId())) {
            throw new Exception("Please login again");
        }

        List<UserProjectMapping> userProjectMappings = userProjectMappingDAO.fetchByProjectIdAndUserIds(
                new ObjectId(createJoinRequestDTO.getProjectId()),
                Arrays.asList(new ObjectId(userBO.getId()))
        );

        if (CollectionUtils.isEmpty(userProjectMappings)) {
            throw new Exception("User can't join files for this project");
        }

        List<File> files = fileDAO.fetchByProjectIdAndId(projects.get(0).getId(),
                Arrays.asList(new ObjectId(createJoinRequestDTO.getFileId1()),
                        new ObjectId(createJoinRequestDTO.getFileId2())));

        if (CollectionUtils.isEmpty(files) || files.size() != 2) {
            throw new Exception("At least one of the files doesn't exist");
        }

        File file1 = null;
        File file2 = null;

        for (File file: files) {
            if (file == null ||
                    StringUtils.isEmpty(file.getFileType()) ||
                    !file.getFileType().equalsIgnoreCase(FileType.META_DATA.name()) ||
                    StringUtils.isEmpty(file.getStatus()) ||
                    !file.getStatus().equalsIgnoreCase(FileStatus.PROCESSED.name()) ||
                    !file.getProjectId().toHexString().equalsIgnoreCase(projects.get(0).getId().toHexString()) ||
                    CollectionUtils.isEmpty(file.getHeaders())) {
                throw new Exception("One of the files is not a PROCESSED META_DATA file" +
                        "or is not a part of this project or doesn't have column headers");
            }

            if (file.getId().toHexString().equalsIgnoreCase(
                    createJoinRequestDTO.getFileId1())) {
                file1 = file;
            }

            if (file.getId().toHexString().equalsIgnoreCase(
                    createJoinRequestDTO.getFileId2())) {
                file2 = file;
            }
        }

        if (file1 == null || file2 == null) {
            throw new Exception("Invalid request");
        }

        for (String column: createJoinRequestDTO.getColumnsForFile1()) {
            if (!file1.getHeaders().contains(column)) {
                throw new Exception(column + " is not a column of file " + file1.getName());
            }
        }

        for (String column: createJoinRequestDTO.getColumnsForFile2()) {
            if (!file2.getHeaders().contains(column)) {
                throw new Exception(column + " is not a column of file " + file2.getName());
            }
        }
    }

    private void checkIfJoinColumnIsInSelectedColumns(List<String> columnsForFile, String joinColumnForFile) throws Exception {
        log.info("Inside checkIfJoinColumnIsInSelectedColumns");

        Boolean wasJoinColumnInSelectedColumns = false;

        if (CollectionUtils.isEmpty(columnsForFile)) {
            throw new Exception("Invalid request");
        }

        for (String column: columnsForFile) {
            if (StringUtils.isEmpty(column)) {
                throw new Exception("Invalid request");
            }

            if (column.equalsIgnoreCase(joinColumnForFile)) {
                wasJoinColumnInSelectedColumns = true;
            }
        }

        if (!wasJoinColumnInSelectedColumns) {
            throw new Exception("Invalid request");
        }
    }
}
