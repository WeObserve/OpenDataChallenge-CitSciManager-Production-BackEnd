package com.sarjom.citisci.services.utilities;

import java.security.NoSuchAlgorithmException;

public interface IHashService {
    String getSha256HexString(String content) throws NoSuchAlgorithmException;
}
