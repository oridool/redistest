package org.redistest;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.protocol.ProtocolVersion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static io.lettuce.core.SocketOptions.DEFAULT_CONNECT_TIMEOUT_DURATION;

@Slf4j
@Configuration
public class RedisConfig {

    @Value("${spring.redis.lettuce.socketOptions.keepalive:false}")
    private boolean redisKeepAlive;

    @Value("${spring.redis.ssl-verify:false}")
    private boolean sslVerify;

    @Primary
    @Bean
    @ConditionalOnProperty(value = "spring.redis.client-type", havingValue = "lettuce", matchIfMissing = true)
    public LettuceConnectionFactory lettuceConnectionFactory(RedisProperties redisProperties) {
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = null;
        if (redisProperties.getLettuce() != null && redisProperties.getLettuce().getPool() != null) {
            log.warn("##### LettuceConnectionFactory: initializing pooling connection, maxActive={} minIdle={}", redisProperties.getLettuce().getPool().getMaxActive(), redisProperties.getLettuce().getPool().getMinIdle());
            LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder poolingBuilder = LettucePoolingClientConfiguration.builder();
            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
            poolConfig.setMaxTotal(redisProperties.getLettuce().getPool().getMaxActive());
            poolConfig.setMaxIdle(redisProperties.getLettuce().getPool().getMaxActive());
            poolConfig.setMinIdle(redisProperties.getLettuce().getPool().getMinIdle());
            poolingBuilder.poolConfig(poolConfig);
            builder = poolingBuilder;
        } else {
            log.warn("##### LettuceConnectionFactory: initializing single connection");
            builder = LettuceClientConfiguration.builder();
        }

        builder.clientOptions(ClientOptions.builder().autoReconnect(true).
                protocolVersion(ProtocolVersion.RESP3)
                .socketOptions(SocketOptions.builder()
                        .connectTimeout(redisProperties.getConnectTimeout() != null ? redisProperties.getConnectTimeout() : DEFAULT_CONNECT_TIMEOUT_DURATION)
                        .keepAlive(SocketOptions.KeepAliveOptions.builder().enable(redisKeepAlive).build())
                        .build())
                .build());

        if (redisProperties.isSsl()) {
            if (!sslVerify) {
                log.warn("##### LettuceConnectionFactory: PeerVerification = FALSE");
                builder.useSsl().disablePeerVerification();
            } else {
                log.warn("##### LettuceConnectionFactory: PeerVerification = TRUE");
                builder.useSsl();
            }
        }

        LettuceClientConfiguration clientConfiguration = builder.build();

        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration();
        standaloneConfig.setHostName(redisProperties.getHost());
        standaloneConfig.setPort(redisProperties.getPort());

        standaloneConfig.setUsername(redisProperties.getUsername());
        standaloneConfig.setPassword(redisProperties.getPassword());
        return new LettuceConnectionFactory(standaloneConfig, clientConfiguration);
    }

    @Primary
    @Bean
    @ConditionalOnProperty(value = "spring.redis.client-type", havingValue = "jedis")
    public JedisConnectionFactory jedisConnectionFactory(RedisProperties redisProperties) {
        JedisClientConfiguration.JedisClientConfigurationBuilder builder = JedisClientConfiguration.builder();
        if (redisProperties.getJedis() != null && redisProperties.getJedis().getPool() != null) {
            log.warn("##### JedisConnectionFactory: initializing pooling connection, maxActive={} minIdle={}", redisProperties.getJedis().getPool().getMaxActive(), redisProperties.getJedis().getPool().getMinIdle());
            JedisClientConfiguration.JedisPoolingClientConfigurationBuilder poolingBuilder = builder.usePooling();
            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
            poolConfig.setMaxTotal(redisProperties.getJedis().getPool().getMaxActive());
            poolConfig.setMaxIdle(redisProperties.getJedis().getPool().getMaxActive());
            poolConfig.setMinIdle(redisProperties.getJedis().getPool().getMinIdle());
            builder.usePooling().poolConfig(poolConfig);
            poolingBuilder.poolConfig(poolConfig);
        } else {
            log.warn("##### JedisConnectionFactory: initializing single connection");
        }

        builder.connectTimeout(redisProperties.getConnectTimeout() != null ? redisProperties.getConnectTimeout() : DEFAULT_CONNECT_TIMEOUT_DURATION);

        if(redisProperties.isSsl()){
            builder.useSsl();
        }

        JedisClientConfiguration clientConfiguration = builder.build();

        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration();
        standaloneConfig.setHostName(redisProperties.getHost());
        standaloneConfig.setPort(redisProperties.getPort());

        standaloneConfig.setUsername(redisProperties.getUsername());
        standaloneConfig.setPassword(redisProperties.getPassword());
        return new JedisConnectionFactory( standaloneConfig, clientConfiguration );
    }

    @Primary
    @Bean
    public RedisTemplate<String,String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String,String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setHashValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setKeySerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

}
