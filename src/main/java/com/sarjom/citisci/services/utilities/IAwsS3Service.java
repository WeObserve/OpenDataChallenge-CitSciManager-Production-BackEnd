package com.sarjom.citisci.services.utilities;

import java.io.File;

public interface IAwsS3Service {
    File downloadFileFromS3(String bucketName, String key) throws Exception;
}
