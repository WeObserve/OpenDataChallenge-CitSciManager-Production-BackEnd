package com.sarjom.citisci.services.utilities;

public interface IAwsSesService {
    void sendEmail(String senderEmail, String recipientEmail, String subject, String message) throws Exception;
}
