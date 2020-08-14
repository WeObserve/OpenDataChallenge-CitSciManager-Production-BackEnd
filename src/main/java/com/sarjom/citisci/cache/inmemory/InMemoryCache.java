package com.sarjom.citisci.cache.inmemory;

import java.util.HashMap;
import java.util.Map;

public class InMemoryCache {
    public static final InMemoryCache inMemoryCache = new InMemoryCache();

    private Map<Class<?>, Object> classToObjectMap = new HashMap<>();

    public static InMemoryCache getInMemoryCache() {
        return inMemoryCache;
    }

    public <T> T get(Class<T> classs) {
        if (classs == null) {
            return null;
        }

        Object cache = classToObjectMap.get(classs);

        if (cache != null) {
            return classs.cast(cache);
        }

        return null;
    }

    public <T> void set(T cache) {
        if (cache == null) {
            return;
        }

        classToObjectMap.put(cache.getClass(), cache);
    }
}
