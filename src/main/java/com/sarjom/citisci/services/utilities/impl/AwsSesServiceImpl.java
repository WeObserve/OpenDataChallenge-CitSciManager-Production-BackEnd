package com.sarjom.citisci.services.utilities.impl;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import com.sarjom.citisci.services.utilities.IAwsSesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AwsSesServiceImpl implements IAwsSesService {
    private static Logger logger = LoggerFactory.getLogger(AwsSesServiceImpl.class);

    @Override
    public void sendEmail(String senderEmail, String recipientEmail, String subject, String message) throws Exception {
        logger.info("Inside sendEmail");

        AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
                .withRegion(Regions.US_WEST_2).build();

        SendEmailRequest request = new SendEmailRequest()
                .withDestination(new Destination().withToAddresses(recipientEmail))
                .withMessage(new Message()
                    .withBody(new Body()
                        .withText(new Content()
                            .withCharset("UTF-8").withData(message)))
                    .withSubject(new Content()
                        .withCharset("UTF-8").withData(subject)))
                .withSource(senderEmail);

        client.sendEmail(request);
    }
}