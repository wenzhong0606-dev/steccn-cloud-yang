package com.steccn.common;

import com.steccn.common.constant.CacheConstant;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import java.time.Duration;
import java.util.Map;
import java.util.Random;

/**
 * @desc 自定义的缓存管理器,用于支持缓存过期时间的自定义
 * @author yequan
 */
@Slf4j
public class TtlRedisCacheManager extends RedisCacheManager {

    Random random = new Random();
    
    public TtlRedisCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration) {
        super(cacheWriter, defaultCacheConfiguration);
    }

    /**
     * 重写createRedisCache方法
     * @param name 原来的name只是作为redis存储键名
     *             重写的name可通过"#"拼接过期时间：
     *             1. 如果没有"#"则默认全局配置的过期时间
     *             2. 拼接的第一个"#"后面为过期时间，第二个"#"后面为时间
     * @param cacheConfig
     * @return
     */
    @Override
    protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
        // 解析name,设置过期时间
        if (StringUtils.isNotEmpty(name) && name.contains("#")) {
            String[] split = name.split("#");
            // 缓存键名
            String cacheName = split[0];
            // "#"后第一位是时间
            try {
                int expire = Integer.parseInt(split[1]);
                //加50s的随机数 避免缓存同时过期
                expire += random.nextInt(expire * CacheConstant.RANDOM_RATE_10 / CacheConstant.PERCENT_100);
                return super.createRedisCache(cacheName, cacheConfig.entryTtl(Duration.ofSeconds(expire)));
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return super.createRedisCache(name, cacheConfig);
    }
}
