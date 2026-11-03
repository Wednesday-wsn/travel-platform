package com.travel.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存工具（以 JSON 字符串形式存储）。
 * <p>
 * 设计要点：
 * 1. 所有方法均做「降级容错」——Redis 不可用或序列化失败时只记录告警日志，
 *    不向上抛异常，保证缓存故障不影响主流程（缓存可降级为直查数据库）。
 * 2. 支持 TypeReference 反序列化，以缓存泛型对象（如分页 Page&lt;BizHotel&gt;、List&lt;T&gt;）。
 * 3. deleteByPattern 用于批量失效某类缓存（如 hotel:list:*）。
 */
@Component
public class RedisUtil {

    private static final Logger log = LoggerFactory.getLogger(RedisUtil.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisUtil(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), timeout, unit);
        } catch (Exception e) {
            log.warn("Redis 写入失败 key={}: {}", key, e.getMessage());
        }
    }

    public <T> T get(String key, Class<T> clazz) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.warn("Redis 读取失败 key={}: {}", key, e.getMessage());
            return null;
        }
    }

    public <T> T get(String key, TypeReference<T> typeRef) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            log.warn("Redis 读取失败 key={}: {}", key, e.getMessage());
            return null;
        }
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis 删除失败 key={}: {}", key, e.getMessage());
        }
    }

    /**
     * 按模式批量删除（如 hotel:list:*）。
     * <p>
     * 说明：keys 命令适合本项目这种中小规模数据；生产环境大数据量下应改用 scan 游标遍历，
     * 避免阻塞 Redis 主线程。
     */
    public void deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("Redis 批量删除失败 pattern={}: {}", pattern, e.getMessage());
        }
    }
}
