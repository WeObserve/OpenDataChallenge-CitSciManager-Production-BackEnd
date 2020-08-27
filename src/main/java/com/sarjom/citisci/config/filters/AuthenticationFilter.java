package com.sarjom.citisci.config.filters;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.sarjom.citisci.bos.UserBO;
import com.sarjom.citisci.cache.inmemory.InMemoryCache;
import com.sarjom.citisci.cache.inmemory.bos.KeyToUserBOMapCacheBO;
import com.sarjom.citisci.cache.inmemory.bos.TokenIdToKeyMapCacheBO;
import com.sarjom.citisci.dtos.ResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.List;

@Component
public class AuthenticationFilter implements Filter {
    private static Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Override
    public void destroy() {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
        throws IOException, ServletException {
        logger.info("Inside doFilter");

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        MDC.put("globalRequestId", UUID.randomUUID().toString());

        logger.info("request: {}", httpServletRequest.getRequestURI());

        List<String> endpointsWithoutAuth = Arrays.asList("/login", "/sign-up-interest");

        if (httpServletRequest.getMethod().equals(HttpMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            MDC.clear();
            return;
        }

        if (endpointsWithoutAuth.contains(httpServletRequest.getRequestURI())) {
            filterChain.doFilter(request, response);
            MDC.clear();
            return;
        }

        String token = httpServletRequest.getHeader("token");
        String tokenId = httpServletRequest.getHeader("tokenId");

        logger.info("Token: {}, tokenId: {}", token, tokenId);

        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(tokenId)) {
            buildErrorResponse(httpServletResponse, "tokenId / token is empty string");
            MDC.clear();
            return;
        }

        TokenIdToKeyMapCacheBO tokenIdToKeyMapCacheBO = InMemoryCache.getInMemoryCache().get(TokenIdToKeyMapCacheBO.class);

        String key = tokenIdToKeyMapCacheBO.get(tokenId);

        logger.info("key: {}", key);

        if (StringUtils.isEmpty(key)) {
            logger.error("No key found");
            buildErrorResponse(httpServletResponse, "Invalid tokenId");
            MDC.clear();
            return;
        }

        try {
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(key)).build().verify(token);
            Map<String, Claim> claims = decodedJWT.getClaims();

            if (CollectionUtils.isEmpty(claims)) {
                logger.error("Invalid token due to jwt decode claims are empty");
                buildErrorResponse(httpServletResponse, "Invalid token");
                MDC.clear();
                return;
            }

            UserBO userBO = InMemoryCache.getInMemoryCache().get(KeyToUserBOMapCacheBO.class).get(key);

            if (userBO == null) {
                logger.error("userBO is null");
                buildErrorResponse(httpServletResponse, "Invalid tokenId");
                MDC.clear();
                return;
            }

            httpServletRequest.setAttribute("user", userBO);
        } catch (Exception e) {
            logger.error("", e);
            buildErrorResponse(httpServletResponse, "Invalid token");
            MDC.clear();
            return;
        }

        filterChain.doFilter(request, response);
        MDC.clear();
    }

    private void buildErrorResponse(HttpServletResponse httpServletResponse, String errorMessage) throws IOException {
        logger.info("Inside buildErrorResponse");

        ResponseDTO<Void> responseDTO = new ResponseDTO<>();

        responseDTO.setStatus("FAILED");
        responseDTO.setReason("Invalid tokenId");

        Gson gson = new Gson();

        httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        httpServletResponse.getWriter().write(gson.toJson(responseDTO));
    }
}
