package hello.service.impl;

import hello.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CacheServiceImpl<T> implements CacheService<T> {
    private final RedisTemplate<String, T> redisTemplate;

    @Autowired
    public CacheServiceImpl(RedisTemplate<String, T> redisTemplate) {
        this.redisTemplate = redisTemplate;
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    }

    public void put(String key, T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public T get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean has(String key) {
        return redisTemplate.hasKey(key);
    }

    public void hput(String hash, String key, T value) {
        redisTemplate.opsForHash().put(hash, key, value);
    }

    public T hget(String hash, String key) {
        return (T) redisTemplate.opsForHash().get(hash, key);
    }

    public boolean hhas(String hash, String key) {
        return redisTemplate.opsForHash().hasKey(hash, key);
    }

    public void hdelete(String hash, String key) {
        redisTemplate.opsForHash().delete(hash, key);
    }

    public List<T> mget(List<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }

    public void mput(Map<String, T> map) {
        redisTemplate.opsForValue().multiSet(map);
    }
}
