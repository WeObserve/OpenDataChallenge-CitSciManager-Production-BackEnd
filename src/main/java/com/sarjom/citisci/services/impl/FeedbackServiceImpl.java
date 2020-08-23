package com.sarjom.citisci.services.impl;

import com.sarjom.citisci.bos.FeedbackBO;
import com.sarjom.citisci.bos.SignUpInterestBO;
import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.db.mongo.daos.IFeedbackDAO;
import com.sarjom.citisci.db.mongo.daos.ISignUpInterestDAO;
import com.sarjom.citisci.dtos.CreateFeedbackRequestDTO;
import com.sarjom.citisci.dtos.CreateFeedbackResponseDTO;
import com.sarjom.citisci.dtos.CreateSignUpInterestRequestDTO;
import com.sarjom.citisci.dtos.CreateSignUpInterestResponseDTO;
import com.sarjom.citisci.entities.Feedback;
import com.sarjom.citisci.entities.SignUpInterest;
import com.sarjom.citisci.services.IFeedbackService;
import com.sarjom.citisci.services.ISignUpInterestService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class FeedbackServiceImpl implements IFeedbackService {
    private static Logger logger = LoggerFactory.getLogger(FeedbackServiceImpl.class);

    @Autowired
    IFeedbackDAO feedbackDAO;

    @Override
    public CreateFeedbackResponseDTO createFeedback(CreateFeedbackRequestDTO createFeedbackRequestDTO, UserBO userBO) throws Exception {
        logger.info("Inside createFeedback");

        validateCreateFeedbackRequestDTO(createFeedbackRequestDTO);

        FeedbackBO feedbackBO = createFeedbackBO(createFeedbackRequestDTO, userBO);

        Feedback feedback = new Feedback();
        BeanUtils.copyProperties(feedbackBO, feedback);

        feedback.setId(new ObjectId(feedbackBO.getId()));
        feedback.setUserId(new ObjectId(feedbackBO.getUserId()));

        feedbackDAO.createFeedback(feedback, null);

        CreateFeedbackResponseDTO createFeedbackResponseDTO = new CreateFeedbackResponseDTO();
        createFeedbackResponseDTO.setCreatedFeedback(feedbackBO);

        return createFeedbackResponseDTO;
    }

    private FeedbackBO createFeedbackBO(CreateFeedbackRequestDTO createFeedbackRequestDTO, UserBO userBO) {
        logger.info("Inside createFeedbackBO");

        FeedbackBO feedbackBO = new FeedbackBO();
        feedbackBO.setId(new ObjectId().toHexString());
        feedbackBO.setComments(createFeedbackRequestDTO.getComments());
        feedbackBO.setUserId(userBO.getId());

        return feedbackBO;
    }

    private void validateCreateFeedbackRequestDTO(CreateFeedbackRequestDTO createFeedbackRequestDTO) throws Exception {
        logger.info("Inside validateCreateFeedbackRequestDTO");

        if (createFeedbackRequestDTO == null ||
                StringUtils.isEmpty(createFeedbackRequestDTO.getComments())) {
            throw new Exception("Invalid request");
        }
    }
}
