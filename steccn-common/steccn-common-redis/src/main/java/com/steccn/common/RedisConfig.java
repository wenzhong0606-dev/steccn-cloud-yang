package com.steccn.common;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * @author yequan
 */
@Configuration
public class RedisConfig extends CachingConfigurerSupport {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public RedisTemplate<String, Integer> redisTemplateInt(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Integer> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericToStringSerializer<>(Integer.class));
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericToStringSerializer<>(Integer.class));
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);

        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public RedisCacheManager redisCacheManager(RedisTemplate redisTemplate) {
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(redisTemplate.getConnectionFactory());
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                //默认设置过期时间为30分钟
                .entryTtl(Duration.ofMinutes(30))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisTemplate.getValueSerializer()));
        TtlRedisCacheManager ttlRedisCacheManager = new TtlRedisCacheManager(redisCacheWriter, redisCacheConfiguration);
        return ttlRedisCacheManager;
    }

    /**
     * 构建基础缓存
     *
     * @return
     */
//    private Map<String, RedisCacheConfiguration> buildInitCaches() {
//        HashMap<String, RedisCacheConfiguration> cacheConfigMap = new HashMap<>();
//        Arrays.stream(applicationContext.getBeanNamesForType(Object.class))
//                .map(applicationContext::getType).filter(Objects::nonNull)
//                .forEach(clazz -> {
//                            ReflectionUtils.doWithMethods(clazz, method -> {
//                                ReflectionUtils.makeAccessible(method);
//                                Cacheable cacheable = AnnotationUtils.findAnnotation(method, Cacheable.class);
//                                if (Objects.nonNull(cacheable)) {
//                                    for (String cache : cacheable.cacheNames()) {
//                                        RedisSerializationContext.SerializationPair<Object> sp = RedisSerializationContext.SerializationPair
//                                                .fromSerializer(new JacksonRedisSerializer<>(method.getGenericReturnType()));
//                                        cacheConfigMap.put(cache, RedisCacheConfiguration.defaultCacheConfig().serializeValuesWith(sp));
//                                    }
//                                }
//                            });
//                        }
//                );
//        return cacheConfigMap;
//    }
}