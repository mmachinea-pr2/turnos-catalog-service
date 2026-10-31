package ar.edu.programacion2.catalog.catalog.infrastructure.redis;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogChanges;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogChangesReadException;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.Assertions.*;

@Testcontainers
class RedisCatalogChangesSourceTests {
    private static final String KEY = "catedra:sync:changes:7";
    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7.4.2-alpine")
        .withExposedPorts(6379);

    @BeforeAll
    static void createReader() throws Exception {
        command("ACL", "SETUSER", "reader", "on", ">test-password", "~catedra:sync:*",
            "+get", "+hello", "+ping", "+client");
    }

    @Test
    void readsAffectedIdsAndIgnoresAdditionalFields() throws Exception {
        command("SET", KEY, """
            {"version":7,"extra":true,"changes":{"professionalCategories":[1],
            "professionals":[15,16],"weeklySchedules":[1],"other":[]}}
            """);
        try (var client = client("test-password")) {
            var source = new RedisCatalogChangesSource(client);
            assertThat(source.read(7)).contains(new CatalogChanges(7, List.of(1L), List.of(15L,16L), List.of(1L)));
            try (var connection = client.connect()) {
                assertThatThrownBy(() -> connection.sync().set(KEY, "overwrite")).hasMessageContaining("NOPERM");
            }
            assertThat(source.read(7)).isPresent();
        }
    }

    @Test
    void distinguishesMissingVersionFromAnEmptyChangeSet() throws Exception {
        command("DEL", KEY);
        try (var client = client("test-password")) {
            var source = new RedisCatalogChangesSource(client);
            assertThat(source.read(7)).isEmpty();
            command("SET", KEY, json("[]"));
            assertThat(source.read(7)).contains(new CatalogChanges(7, List.of(), List.of(), List.of()));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "{}", "[0]", "[-1]", "[15,15]", "[null]", "[true]", "[\"15\"]", "[1.5]", "[9223372036854775808]"})
    void rejectsInvalidIdCollections(String ids) throws Exception {
        command("SET", KEY, json(ids));
        assertInvalid();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "not-json", "null", "[]", "{}",
        "{\"version\":7,\"changes\":null}",
        "{\"version\":7,\"changes\":{\"professionals\":[]}}"})
    void rejectsMalformedOrIncompletePayloads(String json) throws Exception {
        command("SET", KEY, json);
        assertInvalid();
    }

    @ParameterizedTest
    @ValueSource(strings = {"8", "-1", "null", "\"7\"", "7.5", "9223372036854775808"})
    void rejectsMismatchingOrInvalidVersion(String version) throws Exception {
        command("SET", KEY, json("[]").replace("\"version\":7", "\"version\":" + version));
        assertInvalid();
    }

    @Test
    void rejectsTrailingJson() throws Exception {
        command("SET", KEY, json("[]") + " {}");
        assertInvalid();
    }

    @Test
    void rejectsWrongRedisType() throws Exception {
        command("DEL", KEY);
        command("HSET", KEY, "version", "7");
        assertInvalid();
        command("DEL", KEY);
    }

    @Test
    void authenticationFailureIsNotReportedAsMissingVersion() {
        try (var client = client("wrong-password")) {
            assertThatThrownBy(() -> new RedisCatalogChangesSource(client).read(7))
                .isInstanceOf(CatalogChangesReadException.class)
                .hasMessage("Could not read valid catalog changes from Redis").hasNoCause();
        }
    }

    @Test
    void rejectsNegativeRequestedVersion() {
        try (var client = client("test-password")) {
            assertThatIllegalArgumentException().isThrownBy(() -> new RedisCatalogChangesSource(client).read(-1));
        }
    }

    @Test
    void acceptsVersionZero() throws Exception {
        command("SET", "catedra:sync:changes:0", json("[]").replace("\"version\":7", "\"version\":0"));
        try (var client = client("test-password")) {
            assertThat(new RedisCatalogChangesSource(client).read(0))
                .contains(new CatalogChanges(0, List.of(), List.of(), List.of()));
        }
    }

    private void assertInvalid() {
        try (var client = client("test-password")) {
            assertThatThrownBy(() -> new RedisCatalogChangesSource(client).read(7))
                .isInstanceOf(CatalogChangesReadException.class).hasNoCause();
        }
    }

    private static String json(String ids) {
        return "{\"version\":7,\"changes\":{\"professionalCategories\":[],\"professionals\":"
            + ids + ",\"weeklySchedules\":[]}}";
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
