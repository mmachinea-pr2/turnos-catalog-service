package ar.edu.programacion2.catalog.catalog.infrastructure.redis;

import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogVersionSource;
import io.lettuce.core.RedisClient;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogVersionReadException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.assertj.core.api.Assertions.*;

class CatalogRedisConfigurationTests {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(CatalogRedisConfiguration.class);

    @Test
    void disabledByDefault() {
        runner.run(context -> assertThat(context).hasNotFailed()
            .doesNotHaveBean(CatalogVersionSource.class).doesNotHaveBean(RedisClient.class));
    }

    @Test
    void enabledCreatesClientWithoutConnecting() {
        configuredRunner().run(context -> assertThat(context).hasNotFailed()
            .hasSingleBean(CatalogVersionSource.class).hasSingleBean(RedisClient.class));
    }

    @Test
    void unavailableServerProducesSafeFailure() {
        configuredRunner().run(context -> assertThatThrownBy(() -> context.getBean(CatalogVersionSource.class).read())
            .isInstanceOf(CatalogVersionReadException.class).hasNoCause());
    }

    @ParameterizedTest
    @ValueSource(strings = {"host=", "username=", "password=", "port=0", "port=65536", "timeout=0s", "timeout=-1s"})
    void rejectsInvalidConfiguration(String setting) {
        configuredRunner().withPropertyValues("catalog.catedra.redis." + setting)
            .run(context -> assertThat(context).hasFailed());
    }

    private ApplicationContextRunner configuredRunner() {
        return runner.withPropertyValues("catalog.catedra.redis.enabled=true",
            "catalog.catedra.redis.host=127.0.0.1", "catalog.catedra.redis.port=1",
            "catalog.catedra.redis.username=reader", "catalog.catedra.redis.password=test-password",
            "catalog.catedra.redis.timeout=1s");
    }
}
