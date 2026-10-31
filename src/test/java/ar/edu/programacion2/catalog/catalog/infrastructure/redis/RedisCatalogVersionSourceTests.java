package ar.edu.programacion2.catalog.catalog.infrastructure.redis;

import ar.edu.programacion2.catalog.catalog.application.model.RemoteCatalogVersions;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogVersionReadException;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.Duration;
import static org.assertj.core.api.Assertions.*;

@Testcontainers
class RedisCatalogVersionSourceTests {
    private static final String CURRENT = "catedra:sync:current-version";
    private static final String OLDEST = "catedra:sync:oldest-available-version";
    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7.4.2-alpine")
        .withExposedPorts(6379);

    @BeforeAll
    static void createReadOnlyUser() throws Exception {
        command("ACL", "SETUSER", "reader", "on", ">test-password", "~catedra:sync:*",
            "+mget", "+hello", "+ping", "+client");
    }

    @Test
    void readsBothVersionsWithReadOnlyCredentials() throws Exception {
        command("MSET", CURRENT, "7", OLDEST, "4");
        try (var client = client("test-password")) {
            assertThat(new RedisCatalogVersionSource(client).read()).isEqualTo(new RemoteCatalogVersions(7, 4));
            try (var connection = client.connect()) {
                assertThatThrownBy(() -> connection.sync().set(CURRENT, "8")).hasMessageContaining("NOPERM");
            }
            assertThat(new RedisCatalogVersionSource(client).read()).isEqualTo(new RemoteCatalogVersions(7, 4));
        }
    }

    @Test
    void acceptsVersionZero() throws Exception {
        command("MSET", CURRENT, "0", OLDEST, "0");
        try (var client = client("test-password")) {
            assertThat(new RedisCatalogVersionSource(client).read()).isEqualTo(new RemoteCatalogVersions(0, 0));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "", "1.5", " 7", "+7", "9223372036854775808"})
    void rejectsMalformedValues(String value) throws Exception {
        command("MSET", CURRENT, value, OLDEST, "0");
        assertReadFails();
        command("MSET", CURRENT, "7", OLDEST, value);
        assertReadFails();
    }

    @Test
    void rejectsMissingKeysAndWrongTypes() throws Exception {
        command("MSET", CURRENT, "7", OLDEST, "4");
        command("DEL", CURRENT);
        assertReadFails();
        command("SET", CURRENT, "7");
        command("DEL", OLDEST);
        assertReadFails();
        command("HSET", OLDEST, "version", "4");
        assertReadFails();
    }

    @Test
    void rejectsInvertedRange() throws Exception {
        command("MSET", CURRENT, "4", OLDEST, "7");
        assertReadFails();
    }

    @Test
    void authenticationFailureDoesNotExposeConnectionDetails() {
        try (var client = client("wrong-password")) {
            assertThatThrownBy(() -> new RedisCatalogVersionSource(client).read())
                .isInstanceOf(CatalogVersionReadException.class)
                .hasMessage("Could not read valid catalog versions from Redis").hasNoCause();
        }
    }

    private void assertReadFails() {
        try (var client = client("test-password")) {
            assertThatThrownBy(() -> new RedisCatalogVersionSource(client).read())
                .isInstanceOf(CatalogVersionReadException.class).hasNoCause();
        }
    }

    private RedisClient client(String password) {
        return RedisClient.create(RedisURI.Builder.redis(REDIS.getHost(), REDIS.getMappedPort(6379))
            .withAuthentication("reader", password.toCharArray()).withTimeout(Duration.ofSeconds(2)).build());
    }

    private static void command(String... arguments) throws Exception {
        var command = new String[arguments.length + 1];
        command[0] = "redis-cli";
        System.arraycopy(arguments, 0, command, 1, arguments.length);
        var result = REDIS.execInContainer(command);
        assertThat(result.getExitCode()).isZero();
        assertThat(result.getStdout()).doesNotContain("ERR", "WRONGTYPE");
    }
}
