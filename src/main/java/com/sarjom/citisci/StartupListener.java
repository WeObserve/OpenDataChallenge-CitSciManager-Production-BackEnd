package com.sarjom.citisci;

import com.sarjom.citisci.cache.inmemory.InMemoryCache;
import com.sarjom.citisci.cache.inmemory.bos.KeyToUserBOMapCacheBO;
import com.sarjom.citisci.cache.inmemory.bos.TokenIdToKeyMapCacheBO;
import com.sarjom.citisci.cache.inmemory.bos.UserIdToTokenIdMapCacheBO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class StartupListener implements ServletContextListener {
    private static Logger logger = LoggerFactory.getLogger(StartupListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Inside contextInitialized");

        WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext())
                .getAutowireCapableBeanFactory().autowireBean(this);

        TokenIdToKeyMapCacheBO tokenIdToKeyMapCacheBO = new TokenIdToKeyMapCacheBO();
        KeyToUserBOMapCacheBO keyToUserBOMapCacheBO = new KeyToUserBOMapCacheBO();
        UserIdToTokenIdMapCacheBO userIdToTokenIdMapCacheBO = new UserIdToTokenIdMapCacheBO();

        InMemoryCache.getInMemoryCache().set(tokenIdToKeyMapCacheBO);
        InMemoryCache.getInMemoryCache().set(keyToUserBOMapCacheBO);
        InMemoryCache.getInMemoryCache().set(userIdToTokenIdMapCacheBO);
    }
}
