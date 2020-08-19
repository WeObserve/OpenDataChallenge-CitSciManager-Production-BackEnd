package com.sarjom.citisci.services.impl;

import com.sarjom.citisci.bos.SignUpInterestBO;
import com.sarjom.citisci.db.mongo.daos.ISignUpInterestDAO;
import com.sarjom.citisci.dtos.CreateSignUpInterestRequestDTO;
import com.sarjom.citisci.dtos.CreateSignUpInterestResponseDTO;
import com.sarjom.citisci.entities.SignUpInterest;
import com.sarjom.citisci.services.ISignUpInterestService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SignUpInterestServiceImpl implements ISignUpInterestService {
    private static Logger logger = LoggerFactory.getLogger(SignUpInterestServiceImpl.class);

    @Autowired
    ISignUpInterestDAO signUpInterestDAO;

    @Override
    public CreateSignUpInterestResponseDTO createSignUpInterest(CreateSignUpInterestRequestDTO createSignUpInterestRequestDTO) throws Exception {
        logger.info("Inside createSignUpInterest");

        validateCreateSignUpInterestRequestDTO(createSignUpInterestRequestDTO);

        SignUpInterestBO signUpInterestBO = createSignUpInterestBO(createSignUpInterestRequestDTO);

        SignUpInterest signUpInterest = new SignUpInterest();
        BeanUtils.copyProperties(signUpInterestBO, signUpInterest);

        signUpInterest.setId(new ObjectId(signUpInterestBO.getId()));

        signUpInterestDAO.createSignUpInterest(signUpInterest, null);

        CreateSignUpInterestResponseDTO createSignUpInterestResponseDTO = new CreateSignUpInterestResponseDTO();
        createSignUpInterestResponseDTO.setSignUpInterest(signUpInterestBO);

        return createSignUpInterestResponseDTO;
    }

    private SignUpInterestBO createSignUpInterestBO(CreateSignUpInterestRequestDTO createSignUpInterestRequestDTO) {
        logger.info("Inside createSignUpInterestBO");

        SignUpInterestBO signUpInterestBO = new SignUpInterestBO();
        signUpInterestBO.setId(new ObjectId().toHexString());
        signUpInterestBO.setDescription(createSignUpInterestRequestDTO.getDescription());
        signUpInterestBO.setEmailList(createSignUpInterestRequestDTO.getEmailList());

        return signUpInterestBO;
    }

    private void validateCreateSignUpInterestRequestDTO(CreateSignUpInterestRequestDTO createSignUpInterestRequestDTO) throws Exception {
        logger.info("Inside validateCreateSignUpInterestRequestDTO");

        if (createSignUpInterestRequestDTO == null ||
                StringUtils.isEmpty(createSignUpInterestRequestDTO.getDescription()) ||
                StringUtils.isEmpty(createSignUpInterestRequestDTO.getEmailList())) {
            throw new Exception("Invalid request");
        }
    }
}
