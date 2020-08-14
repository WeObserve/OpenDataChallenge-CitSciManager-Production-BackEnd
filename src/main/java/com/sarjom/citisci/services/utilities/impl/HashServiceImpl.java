package com.sarjom.citisci.services.utilities.impl;

import com.sarjom.citisci.services.utilities.IHashService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class HashServiceImpl implements IHashService {
    private static Logger logger = LoggerFactory.getLogger(HashServiceImpl.class);

    @Override
    public String getSha256HexString(String content) throws NoSuchAlgorithmException {
        logger.info("Inside getSha256HexString");

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        return toHexString(md.digest(content.getBytes(StandardCharsets.UTF_8)));
    }

    private String toHexString(byte[] digest) {
        logger.info("Inside toHexString");

        BigInteger number = new BigInteger(1, digest);

        StringBuilder hexString = new StringBuilder(number.toString(16));

        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }
}
