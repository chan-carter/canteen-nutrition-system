package com.health.canteen.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.health.canteen.constant.RedisConstant;
import com.health.canteen.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis配置：键统一用String序列化便于在redis-cli中排查，值用JSON并携带类型信息以便还原为对象，
 * 类型白名单限制只允许本项目与JDK基础类型反序列化，避免多态反序列化被利用
 */
@Slf4j
@Configuration
public class RedisConfiguration {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        GenericJackson2JsonRedisSerializer valueSerializer =
                new GenericJackson2JsonRedisSerializer(cacheObjectMapper());
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 各缓存分别设置过期时间：菜品与分类属于低频变更数据，推荐与统计属于易变数据
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        Map<String, RedisCacheConfiguration> configurations = new HashMap<>();
        configurations.put(RedisConstant.CATEGORY_CACHE, cacheConfiguration(Duration.ofMinutes(30)));
        configurations.put(RedisConstant.DISH_CACHE, cacheConfiguration(Duration.ofMinutes(30)));
        configurations.put(RedisConstant.NUTRITION_CACHE, cacheConfiguration(Duration.ofHours(1)));
        configurations.put(RedisConstant.RECOMMEND_CACHE, cacheConfiguration(Duration.ofMinutes(10)));
        configurations.put(RedisConstant.REPORT_CACHE, cacheConfiguration(Duration.ofMinutes(3)));
        log.info("Redis缓存管理器初始化完成, 缓存: {}", configurations.keySet());
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration(Duration.ofMinutes(10)))
                .withInitialCacheConfigurations(configurations)
                .build();
    }

    private RedisCacheConfiguration cacheConfiguration(Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(cacheObjectMapper())));
    }

    /**
     * 反序列化白名单：STATISTICS 缓存里存的是 Map<String,Object>，NON_FINAL 类型策略会把
     * Long、String 这类包装类型也写入类型ID，因此必须显式放行，否则读缓存时会抛
     * "Could not resolve type id 'java.lang.Long'"。这里只列出实际用到的最终类型，
     * 不整体放行 java.lang.，避免 java.lang.Class、java.lang.Thread 等被反序列化。
     */
    private static final String[] CACHE_TRUSTED_SUBTYPES = {
            "com.health.canteen.",
            "java.lang.String",
            "java.lang.Boolean",
            "java.lang.Character",
            "java.lang.Byte",
            "java.lang.Short",
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Float",
            "java.lang.Double",
            "java.lang.Number",
            "java.util.",
            "java.math.",
            "java.time."
    };

    private ObjectMapper cacheObjectMapper() {
        BasicPolymorphicTypeValidator.Builder builder = BasicPolymorphicTypeValidator.builder();
        for (String subType : CACHE_TRUSTED_SUBTYPES) {
            builder = builder.allowIfSubType(subType);
        }
        ObjectMapper mapper = new JacksonObjectMapper();
        mapper.activateDefaultTyping(builder.build(),
                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        return mapper;
    }

}
