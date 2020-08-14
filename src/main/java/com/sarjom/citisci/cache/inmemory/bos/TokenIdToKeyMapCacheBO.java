package com.sarjom.citisci.cache.inmemory.bos;

import java.util.HashMap;
import java.util.Map;

public class TokenIdToKeyMapCacheBO {
    private static Map<String, String> tokenIdToKeyMap = new HashMap<>();

    public void set(String tokenId, String key) {
        tokenIdToKeyMap.put(tokenId, key);
    }

    public String get(String tokenId) {
        return tokenIdToKeyMap.get(tokenId);
    }
}
