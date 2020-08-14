package com.sarjom.citisci.cache.inmemory.bos;

import com.sarjom.citisci.bos.UserBO;

import java.util.HashMap;
import java.util.Map;

public class KeyToUserBOMapCacheBO {
    private static Map<String, UserBO> keyToUserBOMap = new HashMap<>();

    public void set(String key, UserBO userBO) {
        keyToUserBOMap.put(key, userBO);
    }

    public UserBO get(String key) {
        return keyToUserBOMap.get(key);
    }
}
