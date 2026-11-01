package ar.edu.programacion2.catalog.catalog.infrastructure.redis;

import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogVersionSource;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogChangesSource;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogEntitiesSource;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SocketOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "catalog.catedra.redis.enabled", havingValue = "true")
public class CatalogRedisConfiguration {
    @Bean(destroyMethod = "shutdown")
    RedisClient catalogRedisClient(
            @Value("${catalog.catedra.redis.host:}") String host,
            @Value("${catalog.catedra.redis.port:6379}") int port,
            @Value("${catalog.catedra.redis.username:}") String username,
            @Value("${catalog.catedra.redis.password:}") String password,
            @Value("${catalog.catedra.redis.timeout:5s}") String timeoutValue) {
        var timeout = DurationStyle.detectAndParse(timeoutValue);
        if (host.isBlank() || username.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Redis host and credentials are required");
        }
        if (port < 1 || port > 65535 || timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("Redis port and timeout must be valid");
        }
        var uri = RedisURI.Builder.redis(host, port)
            .withAuthentication(username, password.toCharArray()).withTimeout(timeout).build();
        var client = RedisClient.create(uri);
        client.setOptions(ClientOptions.builder().autoReconnect(false)
            .socketOptions(SocketOptions.builder().connectTimeout(timeout).build()).build());
        return client;
    }

    @Bean
    CatalogEntitiesSource catalogEntitiesSource(RedisClient catalogRedisClient) {
        return new RedisCatalogEntitiesSource(catalogRedisClient);
    }

    @Bean
    CatalogChangesSource catalogChangesSource(RedisClient catalogRedisClient) {
        return new RedisCatalogChangesSource(catalogRedisClient);
    }

    @Bean
    CatalogVersionSource catalogVersionSource(RedisClient catalogRedisClient) {
        return new RedisCatalogVersionSource(catalogRedisClient);
    }
}
