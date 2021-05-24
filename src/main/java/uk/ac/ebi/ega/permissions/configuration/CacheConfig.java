/*
 *
 * Copyright 2021 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.ac.ebi.ega.permissions.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import uk.ac.ebi.ega.permissions.cache.CacheManager;
import uk.ac.ebi.ega.permissions.cache.RedisCacheManager;
import uk.ac.ebi.ega.permissions.cache.serializer.ValueRedisSerializer;
import uk.ac.ebi.ega.permissions.cache.dto.UserDatasetPermissionDTO;

import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Duration.ofSeconds;
import static org.springframework.data.redis.connection.jedis.JedisClientConfiguration.builder;

@Configuration
public class CacheConfig {

    @ConfigurationProperties(prefix = "cache.redis")
    @Bean
    public RedisCacheConfigProperties initCacheRedisConfig() {
        return new RedisCacheConfigProperties();
    }

    @Bean("jedisConnectionFactory")
    public JedisConnectionFactory initJedisConnectionFactory(final RedisCacheConfigProperties properties) {
        final RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(properties.getHost());
        redisStandaloneConfiguration.setPort(properties.getPort());

        final JedisClientConfiguration.JedisClientConfigurationBuilder jedisClientConfiguration = builder();
        jedisClientConfiguration.connectTimeout(ofSeconds(properties.getConnectionTimeout()));

        return new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration.build());
    }

    @Bean("redisTemplateCacheManager")
    public RedisTemplate<String, UserDatasetPermissionDTO> initRedisTemplate(@Qualifier("jedisConnectionFactory") final JedisConnectionFactory factory,
                                                                             @Qualifier("cacheManagerObjectMapper") ObjectMapper objectMapper) {
        final RedisTemplate<String, UserDatasetPermissionDTO> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new ValueRedisSerializer(objectMapper, UTF_8));
        template.setConnectionFactory(factory);
        template.setEnableTransactionSupport(true);
        return template;
    }

    @Bean
    public CacheManager redisCacheManager(@Qualifier("redisTemplateCacheManager") final RedisTemplate<String, UserDatasetPermissionDTO> redisTemplate,
                                          @Value("${cache.redis.namespace}") final String cacheNamespace) {
        return new RedisCacheManager(
                redisTemplate,
                cacheNamespace
        );
    }

    @Bean("cacheManagerObjectMapper")
    public ObjectMapper objectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(SNAKE_CASE);
        return objectMapper;
    }
}
