package com.sarjom.citisci.services.utilities.impl;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import com.sarjom.citisci.services.utilities.IAwsS3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;

@Service
public class AwsS3ServiceImpl implements IAwsS3Service {
    private static Logger logger = LoggerFactory.getLogger(AwsS3ServiceImpl.class);

    @Value("${aws.access.key.id}")
    String accessKeyId;

    @Value("${aws.secret.access.key}")
    String secretAccessKey;

    @Value("${invitation.file.download.path}")
    String userInvitationFileDownloadPath;

    @Override
    public File downloadFileFromS3(String bucketName, String key) throws Exception {
        logger.info("Inside downloadFileFromS3");

        BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(accessKeyId, secretAccessKey);

        AmazonS3 client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
                .withRegion(Regions.US_WEST_2).build();

        S3Object object = client.getObject(bucketName, key);

        S3ObjectInputStream s3ObjectInputStream = object.getObjectContent();

        File file = new File(userInvitationFileDownloadPath + key);
        file.getParentFile().mkdirs();

        FileOutputStream fileOutputStream = new FileOutputStream(file);

        byte[] readBuf = new byte[4096];
        int readLen = 0;

        while ((readLen = s3ObjectInputStream.read(readBuf)) > 0) {
            fileOutputStream.write(readBuf, 0, readLen);
        }

        s3ObjectInputStream.close();
        fileOutputStream.close();

        return file;
    }
}
