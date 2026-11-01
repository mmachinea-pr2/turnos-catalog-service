package ar.edu.programacion2.catalog.catalog.infrastructure.redis;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogChanges;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogEntitiesReadException;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import java.time.Duration;
import java.util.List;
import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@Testcontainers
class RedisCatalogEntitiesSourceTests {
    private static final CatalogChanges CHANGES = new CatalogChanges(7, List.of(1L), List.of(15L), List.of(21L));
    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7.4.2-alpine").withExposedPorts(6379);

    @BeforeAll
    static void createReader() throws Exception {
        command("ACL", "SETUSER", "reader", "on", ">test-password", "~catedra:sync:*",
            "+hmget", "+hello", "+ping", "+client");
    }

    @BeforeEach
    void seed() throws Exception {
        command("DEL", "catedra:sync:professional-categories", "catedra:sync:professionals", "catedra:sync:weekly-schedules");
        command("HSET", "catedra:sync:professional-categories", "1", fixture("professionalCategories"));
        command("HSET", "catedra:sync:professionals", "15", fixture("professionals"));
        command("HSET", "catedra:sync:weekly-schedules", "21", fixture("weeklySchedules"));
    }

    @Test
    void readsAllTypesIncludingLogicalDeletions() {
        try (var client = client("test-password")) {
            var result = new RedisCatalogEntitiesSource(client).read(CHANGES);
            assertThat(result.version()).isEqualTo(7);
            assertThat(result.professionalCategories()).singleElement().satisfies(category -> {
                assertThat(category.id()).isEqualTo(1);
                assertThat(category.description()).isNull();
                assertThat(category.enabled()).isFalse();
            });
            assertThat(result.professionals()).singleElement().satisfies(professional -> {
                assertThat(professional.id()).isEqualTo(15);
                assertThat(professional.categoryId()).isEqualTo(1);
                assertThat(professional.firstName()).isEqualTo("Ana");
                assertThat(professional.enabled()).isFalse();
            });
            assertThat(result.weeklySchedules()).singleElement().satisfies(schedule -> {
                assertThat(schedule.id()).isEqualTo(21);
                assertThat(schedule.startTime()).hasToString("09:00");
                assertThat(schedule.slotDurationMinutes()).isEqualTo(30);
                assertThat(schedule.enabled()).isFalse();
            });
            assertThatThrownBy(() -> result.professionals().clear()).isInstanceOf(UnsupportedOperationException.class);
            try (var connection = client.connect()) {
                assertThatThrownBy(() -> connection.sync().hdel("catedra:sync:professionals", "15"))
                    .hasMessageContaining("NOPERM");
            }
        }
    }

    @Test
    void readsOnlyRequestedEntitiesAndDoesNotRequireUnchangedParents() throws Exception {
        command("DEL", "catedra:sync:professional-categories", "catedra:sync:weekly-schedules");
        command("HSET", "catedra:sync:professionals", "16", fixture("professionals").replace("\"id\":15", "\"id\":16"));
        try (var client = client("test-password")) {
            var result = new RedisCatalogEntitiesSource(client).read(new CatalogChanges(7, List.of(), List.of(16L,15L), List.of()));
            assertThat(result.professionalCategories()).isEmpty();
            assertThat(result.professionals()).extracting(p -> p.id()).containsExactly(16L,15L);
            assertThat(result.weeklySchedules()).isEmpty();
        }
    }

    @Test
    void emptyChangesDoNotConnect() {
        var client = mock(RedisClient.class);
        var result = new RedisCatalogEntitiesSource(client).read(new CatalogChanges(7, List.of(), List.of(), List.of()));
        assertThat(result.professionalCategories()).isEmpty();
        assertThat(result.professionals()).isEmpty();
        assertThat(result.weeklySchedules()).isEmpty();
        verifyNoInteractions(client);
    }

    @ParameterizedTest
    @CsvSource({"professional-categories,1", "professionals,15", "weekly-schedules,21"})
    void missingEntityFailsTheWholeRead(String hash, String id) throws Exception {
        command("HDEL", "catedra:sync:" + hash, id);
        assertInvalid();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "null", "[]", "{}", "not-json"})
    void rejectsInvalidDocuments(String json) throws Exception {
        command("HSET", "catedra:sync:professionals", "15", json);
        assertInvalid();
    }

    @ParameterizedTest
    @CsvSource({"professionalCategories,professional-categories,1", "professionals,professionals,15", "weeklySchedules,weekly-schedules,21"})
    void rejectsMismatchedIds(String field, String hash, String id) throws Exception {
        command("HSET", "catedra:sync:" + hash, id, fixture(field).replace("\"id\":" + id, "\"id\":999"));
        assertInvalid();
    }

    @ParameterizedTest
    @CsvSource({"enabled,\"false\"", "categoryId,0", "categoryId,1.5", "categoryId,\"1\"", "categoryId,9223372036854775808", "firstName,null", "createdAt,\"bad-date\""})
    void rejectsInvalidProfessionalFields(String field, String value) throws Exception {
        var mapper = JsonMapper.builder().build();
        var node = (tools.jackson.databind.node.ObjectNode) mapper.readTree(fixture("professionals"));
        node.set(field, mapper.readTree(value));
        command("HSET", "catedra:sync:professionals", "15", node.toString());
        assertInvalid();
    }

    @ParameterizedTest
    @CsvSource({"dayOfWeek,\"INVALID\"", "startTime,\"bad-time\"", "endTime,\"08:00\"", "slotDurationMinutes,0", "slotDurationMinutes,2147483648", "professionalId,-1"})
    void rejectsInvalidScheduleFields(String field, String value) throws Exception {
        var mapper = JsonMapper.builder().build();
        var node = (tools.jackson.databind.node.ObjectNode) mapper.readTree(fixture("weeklySchedules"));
        node.set(field, mapper.readTree(value));
        command("HSET", "catedra:sync:weekly-schedules", "21", node.toString());
        assertInvalid();
    }

    @Test
    void ignoresUnknownFields() throws Exception {
        command("HSET", "catedra:sync:professional-categories", "1", fixture("professionalCategories").replaceFirst("\\{", "{\"extra\":true,"));
        try (var client = client("test-password")) {
            assertThat(new RedisCatalogEntitiesSource(client).read(CHANGES).professionalCategories()).hasSize(1);
        }
    }

    @Test
    void rejectsTrailingJson() throws Exception {
        command("HSET", "catedra:sync:professionals", "15", fixture("professionals") + " {}");
        assertInvalid();
    }

    @Test
    void rejectsWrongRedisType() throws Exception {
        command("DEL", "catedra:sync:professionals");
        command("SET", "catedra:sync:professionals", "wrong-type");
        assertInvalid();
    }

    @Test
    void authenticationFailureIsSanitized() {
        try (var client = client("wrong-password")) {
            assertThatThrownBy(() -> new RedisCatalogEntitiesSource(client).read(CHANGES))
                .isInstanceOf(CatalogEntitiesReadException.class)
                .hasMessage("Could not read valid catalog entities from Redis").hasNoCause();
        }
    }

    private void assertInvalid() {
        try (var client = client("test-password")) {
            assertThatThrownBy(() -> new RedisCatalogEntitiesSource(client).read(CHANGES))
                .isInstanceOf(CatalogEntitiesReadException.class).hasNoCause();
        }
    }

    private static String fixture(String field) throws IOException {
        try (var input = RedisCatalogEntitiesSourceTests.class.getResourceAsStream("/catalog/snapshot.json")) {
            return JsonMapper.builder().build().readTree(input).path(field).get(0).toString();
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
