package ar.edu.programacion2.catalog;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import static org.assertj.core.api.Assertions.*;

@Testcontainers
class ManualCatalogSynchronizationIntegrationTests {
    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4.8");

    @Test
    void explicitCommandLoadsCatalogOnceAndKeepsItWhenServerFails() throws Exception {
        var status = new AtomicInteger(200);
        var calls = new AtomicInteger();
        var authorization = new AtomicReference<String>();
        byte[] body = new ClassPathResource("catalog/snapshot.json").getContentAsByteArray();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/synchronization/snapshot", exchange -> {
            try (exchange) {
                calls.incrementAndGet();
                authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                byte[] response = status.get() == 200 ? body : "{}".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(status.get(), response.length);
                exchange.getResponseBody().write(response);
            }
        });
        server.start();
        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort();
            try (var context = start(url)) {
                assertThat(context.isActive()).isTrue();
                assertThat(query("SELECT applied_version FROM catalog_sync_state WHERE id=1")).isEqualTo("7");
                assertThat(query("SELECT COUNT(*) FROM weekly_schedule")).isEqualTo("1");
                assertThat(authorization.get()).isEqualTo("Bearer test-token");
            }
            String appliedAt = query("SELECT applied_at FROM catalog_sync_state WHERE id=1");
            try (var context = start(url)) {
                assertThat(context.isActive()).isTrue();
                assertThat(query("SELECT applied_at FROM catalog_sync_state WHERE id=1")).isEqualTo(appliedAt);
            }
            status.set(503);
            assertThatThrownBy(() -> {
                try (var ignored = start(url)) {
                    fail("Startup must fail when synchronization fails");
                }
            }).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Catalog synchronization failed");
            assertThat(query("SELECT applied_version FROM catalog_sync_state WHERE id=1")).isEqualTo("7");
            assertThat(query("SELECT applied_at FROM catalog_sync_state WHERE id=1")).isEqualTo(appliedAt);
            assertThat(query("SELECT COUNT(*) FROM professional_category")).isEqualTo("1");
            assertThat(query("SELECT COUNT(*) FROM professional")).isEqualTo("1");
            assertThat(query("SELECT COUNT(*) FROM weekly_schedule")).isEqualTo("1");
            assertThat(calls.get()).isEqualTo(3);
        } finally {
            server.stop(0);
        }
    }

    private ConfigurableApplicationContext start(String url) {
        return new SpringApplicationBuilder(CatalogApplication.class).web(WebApplicationType.NONE).run(
            "--spring.datasource.url=" + MYSQL.getJdbcUrl(),
            "--spring.datasource.username=" + MYSQL.getUsername(),
            "--spring.datasource.password=" + MYSQL.getPassword(),
            "--catalog.catedra.enabled=true", "--catalog.catedra.base-url=" + url,
            "--catalog.catedra.jwt=test-token", "--catalog.synchronization.run-on-startup=true",
            "--spring.main.banner-mode=off");
    }

    private String query(String sql) throws Exception {
        try (var connection = DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
                var statement = connection.createStatement(); var result = statement.executeQuery(sql)) {
            assertThat(result.next()).isTrue();
            return result.getString(1);
        }
    }
}
