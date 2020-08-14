package com.sarjom.citisci.services.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.cache.inmemory.InMemoryCache;
import com.sarjom.citisci.cache.inmemory.bos.KeyToUserBOMapCacheBO;
import com.sarjom.citisci.cache.inmemory.bos.TokenIdToKeyMapCacheBO;
import com.sarjom.citisci.cache.inmemory.bos.UserIdToTokenIdMapCacheBO;
import com.sarjom.citisci.db.mongo.daos.IUserDAO;
import com.sarjom.citisci.dtos.LoginRequestDTO;
import com.sarjom.citisci.dtos.LoginResponseDTO;
import com.sarjom.citisci.entities.User;
import com.sarjom.citisci.services.ILoginService;
import com.sarjom.citisci.services.utilities.IHashService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LoginServiceImpl implements ILoginService {
    private static Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);

    @Value("${master.key}")
    String masterKey;

    @Autowired
    IUserDAO userDAO;

    @Autowired
    IHashService hashService;

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) throws Exception {
        logger.info("Inside login");

        validateLoginRequestDTO(loginRequestDTO);

        UserBO userBO = checkEmailAndPassword(loginRequestDTO);

        return createLoginResponseDTO(userBO);
    }

    private LoginResponseDTO createLoginResponseDTO(UserBO userBO) {
        logger.info("Inside createLoginResponseDTO");

        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();

        loginResponseDTO.setUser(userBO);

        UserIdToTokenIdMapCacheBO userIdToTokenIdMapCacheBO = InMemoryCache.getInMemoryCache().get(UserIdToTokenIdMapCacheBO.class);
        TokenIdToKeyMapCacheBO tokenIdToKeyMapCacheBO = InMemoryCache.getInMemoryCache().get(TokenIdToKeyMapCacheBO.class);
        KeyToUserBOMapCacheBO keyToUserBOMapCacheBO = InMemoryCache.getInMemoryCache().get(KeyToUserBOMapCacheBO.class);

        if (!StringUtils.isEmpty(userIdToTokenIdMapCacheBO.get(userBO.getId()))) {
            loginResponseDTO.setTokenId(userIdToTokenIdMapCacheBO.get(userBO.getId()));
        } else {
            Algorithm algorithm = Algorithm.HMAC256(masterKey);
            String uuid = UUID.randomUUID().toString();
            Algorithm algorithm1 = Algorithm.HMAC256(uuid);
            String tokenId = JWT.create().withClaim("uuid", uuid).sign(algorithm);
            String token = JWT.create()
                    .withClaim("id", userBO.getId())
                    .withClaim("email", userBO.getEmail())
                    .withClaim("name", userBO.getName())
                    .withClaim("orgName", userBO.getOrgName())
                    .withClaim("orgAffiliation", userBO.getOrgAffiliation())
                    .sign(algorithm1);

            userIdToTokenIdMapCacheBO.set(userBO.getId(), tokenId);
            tokenIdToKeyMapCacheBO.set(tokenId, uuid);
            keyToUserBOMapCacheBO.set(uuid, userBO);

            loginResponseDTO.setTokenId(tokenId);
            loginResponseDTO.setToken(token);

            return loginResponseDTO;
        }

        if (StringUtils.isEmpty(tokenIdToKeyMapCacheBO.get(loginResponseDTO.getTokenId()))) {
            String uuid = UUID.randomUUID().toString();
            Algorithm algorithm1 = Algorithm.HMAC256(uuid);
            String token = JWT.create()
                    .withClaim("id", userBO.getId())
                    .withClaim("email", userBO.getEmail())
                    .withClaim("name", userBO.getName())
                    .withClaim("orgName", userBO.getOrgName())
                    .withClaim("orgAffiliation", userBO.getOrgAffiliation())
                    .sign(algorithm1);

            tokenIdToKeyMapCacheBO.set(loginResponseDTO.getTokenId(), uuid);
            keyToUserBOMapCacheBO.set(uuid, userBO);

            loginResponseDTO.setToken(token);

            return loginResponseDTO;
        }

        String uuid = tokenIdToKeyMapCacheBO.get(loginResponseDTO.getTokenId());
        Algorithm algorithm1 = Algorithm.HMAC256(uuid);
        String token = JWT.create()
                .withClaim("id", userBO.getId())
                .withClaim("email", userBO.getEmail())
                .withClaim("name", userBO.getName())
                .withClaim("orgName", userBO.getOrgName())
                .withClaim("orgAffiliation", userBO.getOrgAffiliation())
                .sign(algorithm1);
        keyToUserBOMapCacheBO.set(uuid, userBO);

        loginResponseDTO.setToken(token);

        return loginResponseDTO;
    }

    private UserBO checkEmailAndPassword(LoginRequestDTO loginRequestDTO) throws Exception {
        logger.info("Inside checkEmailAndPassword");

        List<User> users = userDAO.getUsersByEmail(loginRequestDTO.getEmail());
        User user = users.get(0);

        String jwtEncryptedPassword = loginRequestDTO.getJwtEncryptedPassword();

        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(masterKey)).build().verify(jwtEncryptedPassword);
        Map<String, Claim> claims = decodedJWT.getClaims();

        if (CollectionUtils.isEmpty(claims) || !claims.containsKey("password")) {
            throw new Exception("Password is invalid");
        }

        Claim passwordClaim = claims.get("password");
        String plainTextPassword = passwordClaim.asString();
        String shaHashPassword = hashService.getSha256HexString(plainTextPassword);

        if (!user.getPassword().equalsIgnoreCase(shaHashPassword)) {
            throw new Exception("Incorrect password");
        }

        return convertToUserBO(user);
    }

    private UserBO convertToUserBO(User user) {
        logger.info("Inside convertToUserBO");

        UserBO userBO = new UserBO();
        userBO.setId(user.getId().toHexString());
        userBO.setEmail(user.getEmail());
        userBO.setName(user.getName());
        userBO.setOrgAffiliation(user.getOrgAffiliation());
        userBO.setOrgName(user.getOrgAffiliation());

        return userBO;
    }

    private void validateLoginRequestDTO(LoginRequestDTO loginRequestDTO) throws Exception {
        logger.info("Inside validateLoginRequestDTO");

        if (loginRequestDTO == null || StringUtils.isEmpty(loginRequestDTO.getEmail()) ||
            StringUtils.isEmpty(loginRequestDTO.getJwtEncryptedPassword())) {
            throw new Exception("Invalid request");
        }
    }
}
