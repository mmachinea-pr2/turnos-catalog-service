package ar.edu.programacion2.catalog;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.*;

@Testcontainers
class ManualFullCatalogSynchronizationIntegrationTests {
    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4.8");
    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7.4.2-alpine").withExposedPorts(6379);

    @Test
    void commandRunsSnapshotIncrementRecoveryAndPreservesDataOnFailure() throws Exception {
        command("ACL", "SETUSER", "reader", "on", ">test-password", "~catedra:sync:*",
            "+mget", "+get", "+hmget", "+hello", "+ping", "+client");
        command("MSET", "catedra:sync:current-version", "7", "catedra:sync:oldest-available-version", "1");
        String original = new ClassPathResource("catalog/snapshot.json").getContentAsString(StandardCharsets.UTF_8);
        var body = new AtomicReference<>(original);
        var status = new AtomicInteger(200);
        var calls = new AtomicInteger();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/synchronization/snapshot", exchange -> {
            try (exchange) {
                calls.incrementAndGet();
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                byte[] response = (status.get() == 200 ? body.get() : "{}").getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(status.get(), response.length);
                exchange.getResponseBody().write(response);
            }
        });
        server.start();
        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort();
            runCommand(url);
            assertThat(query("SELECT applied_version FROM catalog_sync_state WHERE id=1")).isEqualTo("7");
            assertThat(query("SELECT COUNT(*) FROM weekly_schedule")).isEqualTo("1");
            assertThat(calls.get()).isEqualTo(1);
            String initialTime = query("SELECT applied_at FROM catalog_sync_state WHERE id=1");
            runCommand(url);
            assertThat(query("SELECT applied_at FROM catalog_sync_state WHERE id=1")).isEqualTo(initialTime);
            assertThat(calls.get()).isEqualTo(1);

            var mapper = JsonMapper.builder().build();
            var category = (tools.jackson.databind.node.ObjectNode) mapper.readTree(original)
                .path("professionalCategories").get(0);
            category.put("name", "Actualizada desde Redis");
            command("HSET", "catedra:sync:professional-categories", "1", category.toString());
            command("SET", "catedra:sync:changes:8", """
                {"version":8,"changes":{"professionalCategories":[1],"professionals":[],"weeklySchedules":[]}}
                """);
            command("SET", "catedra:sync:current-version", "8");
            runCommand(url);
            assertThat(query("SELECT applied_version FROM catalog_sync_state WHERE id=1")).isEqualTo("8");
            assertThat(query("SELECT name FROM professional_category WHERE id=1")).isEqualTo("Actualizada desde Redis");
            assertThat(query("SELECT COUNT(*) FROM professional")).isEqualTo("1");
            assertThat(calls.get()).isEqualTo(1);

            var snapshot = (tools.jackson.databind.node.ObjectNode) mapper.readTree(original);
            snapshot.put("snapshotVersion", 9);
            body.set(snapshot.toString());
            command("SET", "catedra:sync:current-version", "9");
            runCommand(url);
            assertThat(query("SELECT applied_version FROM catalog_sync_state WHERE id=1")).isEqualTo("9");
            assertThat(query("SELECT name FROM professional_category WHERE id=1")).isEqualTo("Clínica");
            assertThat(calls.get()).isEqualTo(2);

            String appliedAt = query("SELECT applied_at FROM catalog_sync_state WHERE id=1");
            command("SET", "catedra:sync:current-version", "10");
            status.set(503);
            assertThatThrownBy(() -> runCommand(url)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Full catalog synchronization failed").hasNoCause();
            assertThat(query("SELECT applied_version FROM catalog_sync_state WHERE id=1")).isEqualTo("9");
            assertThat(query("SELECT applied_at FROM catalog_sync_state WHERE id=1")).isEqualTo(appliedAt);
            assertThat(query("SELECT COUNT(*) FROM weekly_schedule")).isEqualTo("1");
            assertThat(calls.get()).isEqualTo(3);
        } finally {
            server.stop(0);
        }
    }

    private void runCommand(String url) {
        CatalogApplication.main(new String[] {
            "--spring.main.web-application-type=none", "--spring.main.banner-mode=off",
            "--spring.datasource.url=" + MYSQL.getJdbcUrl(),
            "--spring.datasource.username=" + MYSQL.getUsername(),
            "--spring.datasource.password=" + MYSQL.getPassword(),
            "--catalog.catedra.enabled=true", "--catalog.catedra.base-url=" + url,
            "--catalog.catedra.jwt=test-token", "--catalog.catedra.redis.enabled=true",
            "--catalog.catedra.redis.host=" + REDIS.getHost(),
            "--catalog.catedra.redis.port=" + REDIS.getMappedPort(6379),
            "--catalog.catedra.redis.username=reader", "--catalog.catedra.redis.password=test-password",
            "--catalog.synchronization.run-on-startup=false",
            "--catalog.synchronization.full-run-on-startup=true"
        });
    }

    private static void command(String... arguments) throws Exception {
        var command = new String[arguments.length + 1];
        command[0] = "redis-cli";
        System.arraycopy(arguments, 0, command, 1, arguments.length);
        var result = REDIS.execInContainer(command);
        assertThat(result.getExitCode()).isZero();
        assertThat(result.getStdout()).doesNotContain("ERR", "WRONGTYPE");
    }

    private String query(String sql) throws Exception {
        try (var connection = DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
                var statement = connection.createStatement(); var result = statement.executeQuery(sql)) {
            assertThat(result.next()).isTrue();
            return result.getString(1);
        }
    }
}
