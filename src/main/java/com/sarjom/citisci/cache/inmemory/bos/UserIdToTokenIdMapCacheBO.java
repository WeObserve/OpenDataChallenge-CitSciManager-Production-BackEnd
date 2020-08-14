package com.sarjom.citisci.cache.inmemory.bos;

import java.util.HashMap;
import java.util.Map;

public class UserIdToTokenIdMapCacheBO {
    private static Map<String, String> userIdTotokenIdMap = new HashMap<>();

    public void set(String userId, String tokenId) {
        userIdTotokenIdMap.put(tokenId, tokenId);
    }

    public String get(String userId) {
        return userIdTotokenIdMap.get(userId);
    }
}
