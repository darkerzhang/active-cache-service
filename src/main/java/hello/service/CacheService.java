package hello.service;

import java.util.List;
import java.util.Map;

public interface CacheService<T> {
    void put(String key, T value);

    T get(String key);

    void delete(String key);

    boolean has(String key);

    void hput(String hash, String key, T value);

    T hget(String hash, String key);

    void hdelete(String hash, String key);

    boolean hhas(String hash, String key);

    List<T> mget(List<String> keys);

    void mput(Map<String, T> keys);
}
