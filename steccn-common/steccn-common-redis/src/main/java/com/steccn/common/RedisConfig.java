package com.steccn.common;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
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

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * @author yequan
 */
@Configuration
public class RedisConfig extends CachingConfigurerSupport {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public RedisTemplate<String, Long> redisTemplateLong(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Long> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericToStringSerializer<>(Long.class));
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericToStringSerializer<>(Long.class));
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

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // ======================= 时间序列化规则 ===============================
        // yyyy-MM-dd HH:mm:ss
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)));
        // yyyy-MM-dd
        javaTimeModule.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)));
        // HH:mm:ss
        javaTimeModule.addSerializer(LocalTime.class,
                new LocalTimeSerializer(DateTimeFormatter.ofPattern(DatePattern.NORM_TIME_PATTERN)));

        // Instant 类型序列化
        javaTimeModule.addSerializer(Instant.class, InstantSerializer.INSTANCE);

        // ======================= 时间反序列化规则 ==============================
        // yyyy-MM-dd HH:mm:ss
        javaTimeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)));
        // yyyy-MM-dd
        javaTimeModule.addDeserializer(LocalDate.class,
                new LocalDateDeserializer(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)));
        // HH:mm:ss
        javaTimeModule.addDeserializer(LocalTime.class,
                new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DatePattern.NORM_TIME_PATTERN)));
        // Instant 反序列化
        javaTimeModule.addDeserializer(Instant.class, InstantDeserializer.INSTANT);
        objectMapper.registerModule(javaTimeModule);

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