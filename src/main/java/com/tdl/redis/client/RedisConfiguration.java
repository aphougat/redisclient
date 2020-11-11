package com.tdl.redis.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;


@Configuration
@EnableRedisRepositories
public class RedisConfiguration {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.database}")
    private int redisDatabase;

    @Value("${spring.redis.ssl}")
    private boolean ssl;

    @Value("${spring.redis.jedis.pool.max-active}")
    private int maxTotal;

    @Value("${spring.redis.jedis.pool.max-idle}")
    private int maxIdle;

    @Value("${spring.redis.jedis.pool.min-idle}")
    private int minIdle;

    @Value("${spring.redis.connect.timeout}")
    private long connectTimeout;

    @Value("${spring.redis.read.timeout}")
    private long readTimeout;

    @Value("${spring.redis.max.wait.millis}")
    private long maxWaitMillis;

    @Bean
    JedisConnectionFactory jedisConnectionFactory(JedisPoolConfig jedisPoolConfig) {
        Duration readTimeoutDuration = Duration.ofMillis(readTimeout);
        Duration connectTimeoutDuration = Duration.ofMillis(connectTimeout);
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setDatabase(redisDatabase);
        redisStandaloneConfiguration.setPort(port);
        redisStandaloneConfiguration.setPassword(RedisPassword.of(password));
        JedisClientConfiguration.JedisPoolingClientConfigurationBuilder jedisClientConfiguration = JedisClientConfiguration.builder().usePooling();
        jedisClientConfiguration.poolConfig(jedisPoolConfig);
        if (ssl) {
            jedisClientConfiguration.and().clientName("DEMO").connectTimeout(connectTimeoutDuration).readTimeout(readTimeoutDuration).useSsl();
        }else{
            jedisClientConfiguration.and().clientName("DEMO").connectTimeout(connectTimeoutDuration).readTimeout(readTimeoutDuration);
        }
        return new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration.build());
    }

    @Bean
    JedisPoolConfig jedisPoolConfig(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMinIdle(minIdle);
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);

        return jedisPoolConfig;
    }


    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        // Add some specific configuration here. Key serializers, etc.
        return redisTemplate;
    }

    @Bean
    @Qualifier("setOperations")
    public SetOperations<String, String> SetOperations(RedisTemplate<String, String> redisTemplate) {
        return redisTemplate.opsForSet();
    }

}
